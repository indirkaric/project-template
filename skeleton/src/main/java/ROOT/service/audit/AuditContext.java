package {{rootPackage}}.service.audit;

import {{rootPackage}}.service.security.IScopeContext;
import {{rootPackage}}.util.Check;
import {{rootPackage}}.util.StringHelper;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Decides who is recorded as createdBy / updatedBy.
 */
@Component
public final class AuditContext {

    private final IScopeContext scopeContext;

    /**
     * @param scopeContext scope context
     */
    public AuditContext(final IScopeContext scopeContext) {
        Check.isNotNull(scopeContext, "scopeContext");
        this.scopeContext = scopeContext;
    }

    /**
     * The end user forwarded in {@code Sqa-user-id} wins over the direct caller, so a write made by a
     * service on behalf of a user is attributed to the user.
     *
     * @return actor
     */
    public String resolveActor() {
        String forwardedUser = scopeContext.getSqaRequestUserId();
        return StringHelper.isEmpty(forwardedUser) ? scopeContext.getCurrentUserId() : forwardedUser;
    }

    /**
     * @return current time
     */
    public Date now() {
        return new Date();
    }
}
