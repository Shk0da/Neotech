package ee.neotech.javatask.config;

import ee.neotech.javatask.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Arrays;

/**
 * Конфигурация для запуска приложения в режиме CommandLineRunner
 */
@Configuration
@ConditionalOnProperty(value = "spring.commandlinerunner.run", havingValue = "true", matchIfMissing = true)
public class JavaTaskConfiguration implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(JavaTaskConfiguration.class);

    private final TimeService timeService;
    private final ApplicationContext applicationContext;

    public JavaTaskConfiguration(TimeService timeService, ApplicationContext applicationContext) {
        this.timeService = timeService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        log.debug("JavaTask args: {}", Arrays.asList(args));
        if (args.length > 0 && Arrays.asList(args).contains(JavaTaskConstants.SHOW_DATABASE)) {
            log.debug("show database");
            timeService.showDatabaseData();
            System.exit(0);
        }

        log.debug("fire write current time to database");
        timeService.fireWriteCurrentTimeToDatabaseEverySecond();
    }

    @EventListener
    public void onStartup(ApplicationReadyEvent event) {
        shutdownHook();
    }

    @EventListener
    public void onShutdown(ContextStoppedEvent event) {
        log.warn(JavaTaskConstants.SHUTDOWN_MESSAGE);
    }

    /**
     * Хук на корректное завершение приложения (SIGTERM(15))
     */
    @Async
    protected void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            onShutdown(new ContextStoppedEvent(applicationContext));
            SpringApplication.exit(applicationContext);
        }, "shutdown-hook"));
    }
}
