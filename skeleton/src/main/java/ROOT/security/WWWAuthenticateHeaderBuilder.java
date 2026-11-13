package {{rootPackage}}.security;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Builds a Bearer {@code WWW-Authenticate} header value.
 */
public final class WWWAuthenticateHeaderBuilder {

    private WWWAuthenticateHeaderBuilder() {
    }

    /**
     * @param parameters header parameters, in order
     * @return header value
     */
    public static String build(final Map<String, String> parameters) {
        StringJoiner value = new StringJoiner(", ", "Bearer ", "");
        parameters.forEach((key, val) -> value.add(key + "=\"" + val + "\""));
        return value.toString();
    }
}
