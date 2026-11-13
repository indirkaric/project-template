package {{rootPackage}}.db.config;

import {{rootPackage}}.db.model.ReadOnlyDataSourceProperties;
import {{rootPackage}}.db.model.ReadWriteDataSourceProperties;
import {{rootPackage}}.util.Check;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Datasources, entity manager and transaction manager, configured from {@code database:}.
 */
@Configuration
@EnableJpaRepositories(basePackages = "{{rootPackage}}",
        transactionManagerRef = "transactionManager", entityManagerFactoryRef = "entityManager")
@EnableTransactionManagement
public class DataSourceConfigService {

    private final ReadWriteDataSourceProperties readWrite;

    private final ReadOnlyDataSourceProperties readOnly;

    /**
     * @param readWrite read-write properties
     * @param readOnly read-only properties
     */
    public DataSourceConfigService(final ReadWriteDataSourceProperties readWrite,
                                   final ReadOnlyDataSourceProperties readOnly) {
        Check.isNotNull(readWrite, "readWrite");
        Check.isNotNull(readOnly, "readOnly");
        this.readWrite = readWrite;
        this.readOnly = readOnly;
    }

    /**
     * Designed for extension.
     *
     * @return routing datasource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new DataSourceRouting(
                create(readWrite.url(), readWrite.username(), readWrite.password()),
                create(readOnly.url(), readOnly.username(), readOnly.password()));
    }

    /**
     * Designed for extension.
     *
     * @param builder Boot's builder
     * @return entity manager factory scanning {@code entity}
     */
    @Bean(name = "entityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(final EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource()).packages("{{rootPackage}}.entity")
                .jta(false).persistenceUnit("PSQL").build();
    }

    /**
     * Designed for extension.
     *
     * @param entityManager entity manager factory
     * @return transaction manager
     */
    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager(
            @Qualifier("entityManager") final LocalContainerEntityManagerFactoryBean entityManager) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManager.getObject()));
    }

    private static DataSource create(final String url, final String username, final char[] password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, new String(password));
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}
