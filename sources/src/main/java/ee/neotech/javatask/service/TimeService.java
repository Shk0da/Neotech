package ee.neotech.javatask.service;

import ee.neotech.javatask.domain.Time;
import ee.neotech.javatask.repository.TimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static ee.neotech.javatask.config.JavaTaskConstants.DB_RECONNECT_INTERVAL_IN_SECONDS;
import static ee.neotech.javatask.config.JavaTaskConstants.QUEUE_DEFAULT_SIZE;

/**
 * Сервис работает с репозиторием {@link TimeRepository}
 */
@Service
public class TimeService {

    private final Logger log = LoggerFactory.getLogger(TimeService.class);

    private final TaskScheduler taskScheduler;
    private final TimeRepository timeRepository;

    private Queue<Timestamp> timestampQueue = new ArrayBlockingQueue<>(QUEUE_DEFAULT_SIZE);

    private volatile long nextDbCheck = System.currentTimeMillis();

    public TimeService(TaskScheduler taskScheduler, TimeRepository timeRepository) {
        this.taskScheduler = taskScheduler;
        this.timeRepository = timeRepository;
    }

    /**
     * Запуск планировщика для вызова {@link TimeService#writeCurrentTimeToDatabase} раз в 1 секунду
     */
    public void fireWriteCurrentTimeToDatabaseEverySecond() {
        taskScheduler.scheduleWithFixedDelay(this::writeCurrentTimeToDatabase, TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * Вывод содержимого {@link TimeRepository} в {@link TimeService#log}
     */
    public void showDatabaseData() {
        List<Time> databaseData = timeRepository.findAll();
        if (databaseData.isEmpty()) {
            log.info("The database is empty");
        }
        databaseData.forEach(time -> log.info("{} ({})", time.getTimestamp().getTime(), time.getTimestamp()));
    }

    /**
     * Запись текущего timestamp в {@link TimeRepository}
     */
    void writeCurrentTimeToDatabase() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        // если предыдущая попытка записи в бд была неуспешной, и не пришло время для повторной попытки
        // то пишем во временную очередь
        if (timestamp.getTime() < nextDbCheck) {
            log.debug("Next DB check after {} ms", (nextDbCheck - timestamp.getTime()));
            addTimestampToQueue(timestamp);
            return;
        }

        try {
            // выталкиваем из временной очереди значения, если она не пустая
            if (!timestampQueue.isEmpty()) {
                timestampQueue.forEach(item -> {
                    timeRepository.save(new Time(item));
                    timestampQueue.remove();
                    log.debug("Pulled out of the queue and placed in the base: {}", item);
                });
            }

            timeRepository.save(new Time(timestamp));
            log.debug("Save new timestamp: {}", timestamp);
        } catch (TransactionException e) {
            log.warn("No database connection");
            // при недоступности бд или проблем с записью, добавляем timestamp во временную очередь
            addTimestampToQueue(timestamp);
            // устанавливаем время следующей попытки использовать бд
            nextDbCheck = timestamp.getTime() + TimeUnit.SECONDS.toMillis(DB_RECONNECT_INTERVAL_IN_SECONDS);
        }
    }

    /**
     * Сохранение в очередь timestamp
     *
     * @param timestamp {@link Timestamp} для временного хранения
     */
    private void addTimestampToQueue(Timestamp timestamp) {
        if (timestampQueue.size() >= QUEUE_DEFAULT_SIZE * 0.75) {
            timestampQueue = new ArrayBlockingQueue<>((int) (timestampQueue.size() * 1.5), true, timestampQueue);
        }

        timestampQueue.add(timestamp);
        log.debug("Added new timestamp to Queue: {}", timestamp);
    }
}
