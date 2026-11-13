package {{rootPackage}}.db.config;

import {{rootPackage}}.db.constants.DataSourceType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Routes to the read-write pool only inside a read-write transaction; everything else reads.
 */
public final class DataSourceRouting extends AbstractRoutingDataSource {

    /**
     * @param readWrite read-write datasource
     * @param readOnly read-only datasource
     */
    public DataSourceRouting(final DataSource readWrite, final DataSource readOnly) {
        setTargetDataSources(Map.of(DataSourceType.READWRITE, readWrite, DataSourceType.READONLY, readOnly));
        setDefaultTargetDataSource(readOnly);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionDefinitionInterceptor.isCurrentTransactionReadWrite()
                ? DataSourceType.READWRITE
                : DataSourceType.READONLY;
    }
}
