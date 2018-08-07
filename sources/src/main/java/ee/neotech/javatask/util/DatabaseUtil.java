package ee.neotech.javatask.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static ee.neotech.javatask.config.JavaTaskConstants.DB_RECONNECT_INTERVAL_IN_SECONDS;

/**
 * Вспомогательные методы для работы с DB
 */
public final class DatabaseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

    private DatabaseUtil() {
    }

    /**
     * Проверка доступности DataSource
     *
     * @param dataSource {@link DataSource}
     */
    public static void checkDataSource(DataSource dataSource) {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeQuery("select 1");
            LOGGER.info("Connection to the database is established");
        } catch (SQLException e) {
            LOGGER.warn("No database connection");
            try {
                TimeUnit.SECONDS.sleep(DB_RECONNECT_INTERVAL_IN_SECONDS);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            LOGGER.warn("Attempt to re-establish the connection");
            checkDataSource(dataSource);
        }
    }
}
