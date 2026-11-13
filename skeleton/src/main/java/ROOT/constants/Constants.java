package {{rootPackage}}.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

/**
 * Application-wide constants.
 */
public final class Constants {

    public static final String APP_NAME = "{{service-name}}_app";

    public static final String ENTITY_ID = "entityId";

    public static final int LIMIT_PAGE_SIZE = 2000;

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String SQA_REQUEST_ID = "Sqa-request-id";

    public static final String SQA_USER_ID = "Sqa-user-id";

    /** Inbound caller ids, format "&lt;Caller&gt;.&lt;Callee&gt;". */
    public static final String UI_CLIENT = "UI.{{ServiceName}}";

    /** Every inbound caller id granted full authorities by the client-id filter. */
    public static final Set<String> TRUSTED_CLIENTS = Set.of(UI_CLIENT);

    private Constants() {
    }

    /**
     * Keycloak scopes.
     */
    public static final class ApiScopes {

        public static final String SCOPE = "SCOPE_";

        public static final String GENIE_ADMIN_SCOPE = SCOPE + "GENIE-ADMIN";

        public static final String GENIE_MANAGER_SCOPE = SCOPE + "GENIE-MANAGER";

        private ApiScopes() {
        }
    }

    /**
     * {@code @PreAuthorize} expressions and the authority sets behind them.
     */
    public static final class Authority {

        public static final String FULL_AUTHORITIES = "hasAuthority('" + ApiScopes.GENIE_ADMIN_SCOPE + "')";

        public static final String RESTRICTED_AUTHORITIES = "hasAnyAuthority('"
                + ApiScopes.GENIE_MANAGER_SCOPE + "','" + ApiScopes.GENIE_ADMIN_SCOPE + "')";

        private Authority() {
        }

        /**
         * Authorities granted to a trusted service-to-service caller.
         *
         * @return authorities
         */
        public static List<SimpleGrantedAuthority> fullAuthorities() {
            return List.of(new SimpleGrantedAuthority(ApiScopes.GENIE_ADMIN_SCOPE),
                    new SimpleGrantedAuthority(ApiScopes.GENIE_MANAGER_SCOPE));
        }
    }
}
