package {{rootPackage}}.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import {{rootPackage}}.exception.response.RestApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 403 with an RFC 6750 {@code WWW-Authenticate} header.
 */
public final class CustomOAuth2AccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOAuth2AccessDeniedHandler.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
                       final AccessDeniedException exception) throws IOException {
        LOGGER.error(exception.getMessage(), exception);

        Map<String, String> parameters = new LinkedHashMap<>();
        if (request.getUserPrincipal() instanceof AbstractOAuth2TokenAuthenticationToken) {
            parameters.put("error", "insufficient_scope");
            parameters.put("error_description",
                    "The request requires higher privileges than provided by the access token.");
        }

        response.addHeader("WWW-Authenticate", WWWAuthenticateHeaderBuilder.build(parameters));
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().println(
                OBJECT_MAPPER.writeValueAsString(new RestApiResponse(exception.getMessage())));
    }
}
