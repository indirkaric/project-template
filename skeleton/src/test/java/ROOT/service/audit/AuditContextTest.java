package {{rootPackage}}.service.audit;

import {{rootPackage}}.service.security.IScopeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditContextTest {

    @Mock
    private IScopeContext scopeContext;

    private AuditContext auditContext;

    @BeforeEach
    void setUp() {
        auditContext = new AuditContext(scopeContext);
    }

    @Test
    void shouldPreferForwardedUser() {
        when(scopeContext.getSqaRequestUserId()).thenReturn("U000001");

        assertThat(auditContext.resolveActor()).isEqualTo("U000001");
    }

    @Test
    void shouldFallBackToDirectCaller() {
        when(scopeContext.getSqaRequestUserId()).thenReturn(null);
        when(scopeContext.getCurrentUserId()).thenReturn("UI.Service");

        assertThat(auditContext.resolveActor()).isEqualTo("UI.Service");
        assertThat(auditContext.now()).isNotNull();
    }

    @Test
    void shouldRejectNullScopeContext() {
        assertThatThrownBy(() -> new AuditContext(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
