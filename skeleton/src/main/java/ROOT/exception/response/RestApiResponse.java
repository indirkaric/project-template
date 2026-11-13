package {{rootPackage}}.exception.response;

/**
 * Body of a single-message error.
 *
 * @param message message
 */
public record RestApiResponse(String message) {
}
