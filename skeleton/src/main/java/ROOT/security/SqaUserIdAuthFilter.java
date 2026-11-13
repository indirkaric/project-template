package {{rootPackage}}.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import {{rootPackage}}.util.Check;
import {{rootPackage}}.util.StringHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decodes {@code Sqa-user-id} (base64 JSON with {@code userId}) into {@link IScopeContext}.
 */
public final class SqaUserIdAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqaUserIdAuthFilter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final IScopeContext scopeContext;

    /**
     * @param scopeContext scope context
     */
    public SqaUserIdAuthFilter(final IScopeContext scopeContext) {
        Check.isNotNull(scopeContext, "scopeContext");
        this.scopeContext = scopeContext;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = decodeUserId(request.getHeader(Constants.SQA_USER_ID));
            if (!StringHelper.isEmpty(userId)) {
                scopeContext.setSqaUserId(userId);
            }

            filterChain.doFilter(request, response);
        } finally {
            scopeContext.clearSqaUserId();
        }
    }

    private String decodeUserId(final String header) {
        if (StringHelper.isEmpty(header)) {
            return null;
        }

        try {
            String json = new String(Base64.getDecoder().decode(header), StandardCharsets.UTF_8);
            return OBJECT_MAPPER.readTree(json).path("userId").asText(null);
        } catch (IllegalArgumentException | IOException e) {
            // A malformed header must not turn into a 500; the request continues without a user id.
            LOGGER.warn("Ignoring malformed {} header", Constants.SQA_USER_ID, e);
            return null;
        }
    }
}
