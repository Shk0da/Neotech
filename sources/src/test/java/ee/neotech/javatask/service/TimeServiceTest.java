package ee.neotech.javatask.service;

import ee.neotech.javatask.domain.Time;
import ee.neotech.javatask.repository.TimeRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Тесты для {@link TimeService}
 */
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeServiceTest {

    private static final int TEST_DATA_COUNT = 10;

    private final Logger log = LoggerFactory.getLogger(TimeServiceTest.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private TimeService timeService;

    @Value("${logging.file}")
    private String loggingFile;

    @Before
    public void setUp() {
        timeService = new TimeService(taskScheduler, timeRepository);
    }

    /**
     * Проверка успешной поочередной вставки
     */
    @Test
    public void test1WriteCurrentTimeToDatabase() throws InterruptedException {
        for (int i = 0; i < TEST_DATA_COUNT; i++) {
            TimeUnit.SECONDS.sleep(1);
            timeService.writeCurrentTimeToDatabase();
        }
        Assert.assertEquals(TEST_DATA_COUNT, timeRepository.count());
    }

    /**
     * При выводе данных на экран timestamp должен быть в Ascending Order без применения сортировки
     */
    @Test
    public void test2ShowDatabaseDataTest() {
        timeService.showDatabaseData();
        try {
            List<String> logData = Files.readAllLines(new File(loggingFile).toPath());
            List<String> logShowDatabaseData = logData.subList(logData.size() - TEST_DATA_COUNT, logData.size());
            int row = 0;
            long prevTimestamp = 0;
            for (Time time : timeRepository.findAll()) {
                long currentTimestamp = time.getTimestamp().getTime();
                // наличие вывода в логе
                Assert.assertTrue(logShowDatabaseData.get(row++).contains(String.valueOf(currentTimestamp)));
                // записи раположены по возрастанию
                Assert.assertTrue(currentTimestamp > prevTimestamp);
                prevTimestamp = currentTimestamp;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("test2ShowDatabaseDataTest failed");
        }
    }
}