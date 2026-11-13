package {{rootPackage}}.security;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import {{rootPackage}}.util.Check;
import {{rootPackage}}.util.StringHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Reads or generates {@code Sqa-request-id} and keeps it on {@link IScopeContext} for the request.
 */
public final class SqaRequestIdHeaderFilter extends OncePerRequestFilter {

    private final IScopeContext scopeContext;

    /**
     * @param scopeContext scope context
     */
    public SqaRequestIdHeaderFilter(final IScopeContext scopeContext) {
        Check.isNotNull(scopeContext, "scopeContext");
        this.scopeContext = scopeContext;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(Constants.SQA_REQUEST_ID);
        scopeContext.setCorrelationId(StringHelper.isEmpty(requestId) ? UUID.randomUUID().toString() : requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            scopeContext.clearCorrelationId();
        }
    }

    // The async re-dispatch never re-enters doFilterInternal, so it cannot clear the id the
    // original dispatch set for the async thread.
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }
}
