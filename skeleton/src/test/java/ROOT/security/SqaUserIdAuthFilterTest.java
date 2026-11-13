package {{rootPackage}}.security;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqaUserIdAuthFilterTest {

    @Mock
    private IScopeContext scopeContext;

    @Mock
    private FilterChain chain;

    private SqaUserIdAuthFilter filter;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        filter = new SqaUserIdAuthFilter(scopeContext);
        request = new MockHttpServletRequest();
    }

    @Test
    void shouldStoreDecodedUserIdAndClearIt() throws Exception {
        request.addHeader(Constants.SQA_USER_ID, Base64.getEncoder()
                .encodeToString("{\"userId\":\"U000001\"}".getBytes(StandardCharsets.UTF_8)));

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        verify(scopeContext).setSqaUserId("U000001");
        verify(chain).doFilter(eq(request), any());
        verify(scopeContext).clearSqaUserId();
    }

    @Test
    void shouldIgnoreMissingHeader() throws Exception {
        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        verify(scopeContext, never()).setSqaUserId(anyString());
        verify(scopeContext).clearSqaUserId();
    }

    @Test
    void shouldIgnoreMalformedHeader() throws Exception {
        request.addHeader(Constants.SQA_USER_ID, "%%%not-base64");

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        verify(scopeContext, never()).setSqaUserId(anyString());
    }

    @Test
    void shouldRejectNullScopeContext() {
        assertThatThrownBy(() -> new SqaUserIdAuthFilter(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
