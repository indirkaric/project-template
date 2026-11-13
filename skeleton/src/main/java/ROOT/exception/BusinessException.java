package {{rootPackage}}.exception;

import {{rootPackage}}.util.Check;

import java.util.List;

/**
 * A violated business rule, mapped to 422.
 */
public final class BusinessException extends RuntimeException {

    private final List<String> messages;

    /**
     * @param message rule violation
     */
    public BusinessException(final String message) {
        super(message);
        Check.isNotEmpty(message, "message");
        this.messages = List.of(message);
    }

    /**
     * @param messages rule violations
     */
    public BusinessException(final List<String> messages) {
        super(String.join("; ", messages));
        Check.isNotEmpty(messages, "messages");
        this.messages = List.copyOf(messages);
    }

    /**
     * @return rule violations
     */
    public List<String> getMessages() {
        return messages;
    }
}
