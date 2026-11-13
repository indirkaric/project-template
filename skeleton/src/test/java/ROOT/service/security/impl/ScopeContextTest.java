package {{rootPackage}}.service.security.impl;

import {{rootPackage}}.constants.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeContextTest {

    private ScopeContext scopeContext;

    @BeforeEach
    void setUp() {
        scopeContext = new ScopeContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        scopeContext.clearCorrelationId();
        scopeContext.clearSqaUserId();
    }

    @Test
    void shouldReturnAppNameWithoutAuthentication() {
        assertThat(scopeContext.getCurrentUser()).isEqualTo(Constants.APP_NAME);
        assertThat(scopeContext.getCurrentUserId()).isEqualTo(Constants.APP_NAME);
    }

    @Test
    void shouldReturnCallerIdForServiceCall() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(Constants.UI_CLIENT, null, List.of()));

        assertThat(scopeContext.getCurrentUserId()).isEqualTo(Constants.UI_CLIENT);
    }

    @Test
    void shouldReadClaimsFromJwt() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
                .claim("email", "a@b.c").claim("user_id", "U000001").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(scopeContext.getCurrentUser()).isEqualTo("a@b.c");
        assertThat(scopeContext.getCurrentUserId()).isEqualTo("U000001");
    }

    @Test
    void shouldStoreAndClearThreadValues() {
        scopeContext.setCorrelationId("corr");
        scopeContext.setSqaUserId("U000001");

        assertThat(scopeContext.getCorrelationId()).isEqualTo("corr");
        assertThat(scopeContext.getSqaRequestUserId()).isEqualTo("U000001");

        scopeContext.clearCorrelationId();
        scopeContext.clearSqaUserId();

        assertThat(scopeContext.getCorrelationId()).isNull();
        assertThat(scopeContext.getSqaRequestUserId()).isNull();
    }

    @Test
    void shouldEncodeForwardedUserAsBase64Json() {
        scopeContext.setSqaUserId("U000001");

        String decoded = new String(Base64.getDecoder().decode(scopeContext.getBase64EncodedUserInfo()),
                StandardCharsets.UTF_8);

        assertThat(decoded).isEqualTo("{\"userId\":\"U000001\"}");
    }
}
