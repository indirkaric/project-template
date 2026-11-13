package {{rootPackage}}.util;

import java.util.Collection;

/**
 * Defensive parameter checks. Every failure is an {@link IllegalArgumentException}, mapped to 400.
 */
public final class Check {

    private Check() {
    }

    /**
     * Fails when the value is null or blank.
     *
     * @param parameterValue value
     * @param parameterName name used in the message
     */
    public static void isNotEmpty(final String parameterValue, final String parameterName) {
        isNotNull(parameterValue, parameterName);
        if (parameterValue.isBlank()) {
            throw new IllegalArgumentException(emptyMessage(parameterName));
        }
    }

    /**
     * Fails when the array is null or empty.
     *
     * @param parameterValue value
     * @param parameterName name used in the message
     */
    public static void isNotEmpty(final char[] parameterValue, final String parameterName) {
        isNotNull(parameterValue, parameterName);
        if (parameterValue.length == 0) {
            throw new IllegalArgumentException(emptyMessage(parameterName));
        }
    }

    /**
     * Fails when the collection is null or empty.
     *
     * @param parameterValue value
     * @param parameterName name used in the message
     */
    public static void isNotEmpty(final Collection<?> parameterValue, final String parameterName) {
        isNotNull(parameterValue, parameterName);
        if (parameterValue.isEmpty()) {
            throw new IllegalArgumentException(emptyMessage(parameterName));
        }
    }

    /**
     * Fails when the value is null.
     *
     * @param parameterValue value
     * @param parameterName name used in the message
     * @param <T> value type
     */
    public static <T> void isNotNull(final T parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(String.format("Parameter named %s is null.", parameterName));
        }
    }

    private static String emptyMessage(final String parameterName) {
        return String.format("Parameter named %s is empty.", parameterName);
    }
}
