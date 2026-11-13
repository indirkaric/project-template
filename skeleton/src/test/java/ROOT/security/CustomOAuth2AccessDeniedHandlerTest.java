package {{rootPackage}}.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class CustomOAuth2AccessDeniedHandlerTest {

    private final CustomOAuth2AccessDeniedHandler handler = new CustomOAuth2AccessDeniedHandler();

    @Test
    void shouldAnswer403WithInsufficientScopeForJwtCaller() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new JwtAuthenticationToken(
                Jwt.withTokenValue("t").header("alg", "none").claim("sub", "u").build()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getHeader("WWW-Authenticate")).startsWith("Bearer error=\"insufficient_scope\"");
        assertThat(response.getContentAsString()).contains("\"message\":\"denied\"");
    }

    @Test
    void shouldAnswer403WithPlainBearerForOtherCallers() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer ");
    }
}
