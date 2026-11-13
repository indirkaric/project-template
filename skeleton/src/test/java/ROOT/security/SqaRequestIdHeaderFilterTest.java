package {{rootPackage}}.security;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqaRequestIdHeaderFilterTest {

    @Mock
    private IScopeContext scopeContext;

    @Mock
    private FilterChain chain;

    private SqaRequestIdHeaderFilter filter;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        filter = new SqaRequestIdHeaderFilter(scopeContext);
        request = new MockHttpServletRequest();
    }

    @Test
    void shouldUseIncomingRequestId() throws Exception {
        request.addHeader(Constants.SQA_REQUEST_ID, "corr");

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        verify(scopeContext).setCorrelationId("corr");
        verify(scopeContext).clearCorrelationId();
    }

    @Test
    void shouldGenerateRequestIdWhenMissing() throws Exception {
        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        verify(scopeContext).setCorrelationId(matches("[0-9a-f-]{36}"));
    }

    @Test
    void shouldClearRequestIdWhenChainFails() throws Exception {
        doThrow(new ServletException("boom")).when(chain).doFilter(any(), any());

        assertThatThrownBy(() -> filter.doFilterInternal(request, new MockHttpServletResponse(), chain))
                .isInstanceOf(ServletException.class);
        verify(scopeContext).clearCorrelationId();
    }

    @Test
    void shouldSkipAsyncDispatch() {
        assertThat(filter.shouldNotFilterAsyncDispatch()).isTrue();
    }

    @Test
    void shouldRejectNullScopeContext() {
        assertThatThrownBy(() -> new SqaRequestIdHeaderFilter(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
