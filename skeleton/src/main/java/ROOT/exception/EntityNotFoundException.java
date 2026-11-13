package {{rootPackage}}.exception;

import {{rootPackage}}.util.Check;

/**
 * A lookup found nothing, mapped to 404.
 */
public final class EntityNotFoundException extends RuntimeException {

    /**
     * @param message what was not found
     */
    public EntityNotFoundException(final String message) {
        super(message);
        Check.isNotEmpty(message, "message");
    }
}
