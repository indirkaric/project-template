package {{rootPackage}}.exception;

import {{rootPackage}}.exception.response.BusinessExceptionResponse;
import {{rootPackage}}.exception.response.RestApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * Maps exceptions to HTTP responses. Every handler logs before responding.
 */
@ControllerAdvice
public final class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * @param exception exception
     * @return 404
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RestApiResponse> handle(final EntityNotFoundException exception) {
        LOGGER.warn(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * @param exception exception
     * @return 422
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BusinessExceptionResponse> handle(final BusinessException exception) {
        LOGGER.warn(exception.getMessage(), exception);
        return new ResponseEntity<>(new BusinessExceptionResponse(exception.getMessages()),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * @param exception exception thrown by {@code Check}
     * @return 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BusinessExceptionResponse> handle(final IllegalArgumentException exception) {
        LOGGER.warn(exception.getMessage(), exception);
        return new ResponseEntity<>(new BusinessExceptionResponse(List.of(exception.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * @param exception bean validation failure
     * @return 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BusinessExceptionResponse> handle(final MethodArgumentNotValidException exception) {
        LOGGER.warn(exception.getMessage(), exception);
        List<String> messages = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return new ResponseEntity<>(new BusinessExceptionResponse(messages), HttpStatus.BAD_REQUEST);
    }

    /**
     * Re-throws security exceptions so Spring Security answers them, keeps the status of Spring MVC's
     * own exceptions (404 for an unknown path, 405, 415, ...), and turns anything else into a 500.
     *
     * @param exception exception
     * @return error response
     * @throws Exception the security exception, unchanged
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiResponse> handle(final Exception exception) throws Exception {
        if (exception instanceof AuthenticationException || exception instanceof AccessDeniedException) {
            throw exception;
        }

        if (exception instanceof ErrorResponse errorResponse) {
            LOGGER.warn(exception.getMessage(), exception);
            return new ResponseEntity<>(new RestApiResponse(errorResponse.getBody().getDetail()),
                    errorResponse.getStatusCode());
        }

        LOGGER.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new RestApiResponse("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
