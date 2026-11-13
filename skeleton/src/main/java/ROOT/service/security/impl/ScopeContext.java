package {{rootPackage}}.service.security.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.dto.UserInfoDTO;
import {{rootPackage}}.service.security.IScopeContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * ThreadLocal-backed {@link IScopeContext}; the filters set and clear the values per request.
 */
@Service("scopeContext")
public final class ScopeContext implements IScopeContext {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getCurrentUser() {
        return principalValue("email");
    }

    @Override
    public String getCurrentUserId() {
        return principalValue("user_id");
    }

    @Override
    public void setCorrelationId(final String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    @Override
    public String getCorrelationId() {
        return CORRELATION_ID.get();
    }

    @Override
    public void clearCorrelationId() {
        CORRELATION_ID.remove();
    }

    @Override
    public void setSqaUserId(final String userId) {
        USER_ID.set(userId);
    }

    @Override
    public String getSqaRequestUserId() {
        return USER_ID.get();
    }

    @Override
    public void clearSqaUserId() {
        USER_ID.remove();
    }

    @Override
    public String getBase64EncodedUserInfo() {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(new UserInfoDTO(getSqaRequestUserId()));
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize user info", e);
        }
    }

    private String principalValue(final String jwtClaim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // No authentication at all means a scheduled job or startup code, not a request.
        if (authentication == null) {
            return Constants.APP_NAME;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString(jwtClaim);
        }

        return String.valueOf(principal);
    }
}
