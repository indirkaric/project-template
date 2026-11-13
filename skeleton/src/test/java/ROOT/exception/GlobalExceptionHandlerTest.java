package {{rootPackage}}.exception;

import {{rootPackage}}.exception.response.BusinessExceptionResponse;
import {{rootPackage}}.exception.response.RestApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldMapNotFoundTo404() {
        ResponseEntity<RestApiResponse> response = handler.handle(new EntityNotFoundException("missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new RestApiResponse("missing"));
    }

    @Test
    void shouldMapBusinessExceptionTo422() {
        ResponseEntity<BusinessExceptionResponse> response = handler.handle(new BusinessException("rule"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().messages()).containsExactly("rule");
    }

    @Test
    void shouldMapIllegalArgumentTo400() {
        ResponseEntity<BusinessExceptionResponse> response = handler.handle(new IllegalArgumentException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().messages()).containsExactly("bad");
    }

    @Test
    void shouldMapBeanValidationTo400WithFieldMessages() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));
        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("shouldMapBeanValidationTo400WithFieldMessages"), -1);

        ResponseEntity<BusinessExceptionResponse> response =
                handler.handle(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().messages()).containsExactly("name: must not be blank");
    }

    @Test
    void shouldMapUnexpectedExceptionTo500() throws Exception {
        ResponseEntity<RestApiResponse> response = handler.handle(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Something went wrong");
    }

    @Test
    void shouldKeepStatusOfSpringMvcExceptions() throws Exception {
        ResponseEntity<RestApiResponse> response =
                handler.handle(new NoResourceFoundException(HttpMethod.GET, "missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRethrowSecurityExceptions() {
        assertThatThrownBy(() -> handler.handle((Exception) new AccessDeniedException("no")))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> handler.handle((Exception) new BadCredentialsException("no")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
