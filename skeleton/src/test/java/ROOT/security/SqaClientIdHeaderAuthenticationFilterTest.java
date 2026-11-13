package {{rootPackage}}.security;

import {{rootPackage}}.constants.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class SqaClientIdHeaderAuthenticationFilterTest {

    private SqaClientIdHeaderAuthenticationFilter filter;

    private MockHttpServletRequest request;

    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SqaClientIdHeaderAuthenticationFilter();
        request = new MockHttpServletRequest();
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGrantFullAuthoritiesToTrustedClient() throws Exception {
        request.addHeader(Constants.SQA_CLIENT_ID_HEADER, Constants.UI_CLIENT);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(Constants.UI_CLIENT);
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly(Constants.ApiScopes.GENIE_ADMIN_SCOPE, Constants.ApiScopes.GENIE_MANAGER_SCOPE);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldGrantNothingToUnknownClient() throws Exception {
        request.addHeader(Constants.SQA_CLIENT_ID_HEADER, "Unknown.Client");

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
    }

    @Test
    void shouldLeaveContextEmptyWithoutHeader() throws Exception {
        filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }
}
