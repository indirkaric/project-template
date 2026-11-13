package {{rootPackage}}.db.model;

import {{rootPackage}}.util.Check;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties under {@code database.readonly}.
 *
 * @param url JDBC url
 * @param username user
 * @param password password
 */
@ConfigurationProperties(prefix = "database.readonly")
public record ReadOnlyDataSourceProperties(String url, String username, char[] password) {

    /**
     * Fails fast on missing configuration.
     */
    public ReadOnlyDataSourceProperties {
        Check.isNotEmpty(url, "database.readonly.url");
        Check.isNotEmpty(username, "database.readonly.username");
        Check.isNotEmpty(password, "database.readonly.password");
    }
}
