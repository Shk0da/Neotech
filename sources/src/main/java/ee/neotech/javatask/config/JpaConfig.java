package ee.neotech.javatask.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ee.neotech.javatask.util.DatabaseUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Конфигурация источника данных DB
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@ConditionalOnProperty(value = "spring.commandlinerunner.run", havingValue = "true", matchIfMissing = true)
public class JpaConfig extends HikariConfig {

    public JpaConfig(Environment env) {
        setJdbcUrl(env.getProperty("spring.datasource.url"));
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(getDriverClassName());
        driverManagerDataSource.setUrl(getJdbcUrl());
        driverManagerDataSource.setUsername(getUsername());
        driverManagerDataSource.setPassword(getPassword());
        DatabaseUtil.checkDataSource(driverManagerDataSource);

        HikariDataSource hikariDataSource = new HikariDataSource(this);
        hikariDataSource.setConnectionTimeout(250);
        return hikariDataSource;
    }
}