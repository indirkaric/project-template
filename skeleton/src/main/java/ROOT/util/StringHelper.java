package {{rootPackage}}.util;

/**
 * String helpers.
 */
public final class StringHelper {

    private StringHelper() {
    }

    /**
     * True for null or blank.
     *
     * @param value value
     * @return boolean
     */
    public static boolean isEmpty(final String value) {
        return value == null || value.isBlank();
    }
}
