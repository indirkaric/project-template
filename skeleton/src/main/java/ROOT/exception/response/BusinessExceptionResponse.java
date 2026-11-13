package {{rootPackage}}.exception.response;

import java.util.List;

/**
 * Body of a 400 or 422.
 *
 * @param messages messages
 */
public record BusinessExceptionResponse(List<String> messages) {
}
