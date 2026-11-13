package {{rootPackage}}.db.model;

import {{rootPackage}}.util.Check;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties under {@code database.readwrite}.
 *
 * @param url JDBC url
 * @param username user
 * @param password password
 */
@ConfigurationProperties(prefix = "database.readwrite")
public record ReadWriteDataSourceProperties(String url, String username, char[] password) {

    /**
     * Fails fast on missing configuration.
     */
    public ReadWriteDataSourceProperties {
        Check.isNotEmpty(url, "database.readwrite.url");
        Check.isNotEmpty(username, "database.readwrite.username");
        Check.isNotEmpty(password, "database.readwrite.password");
    }
}
