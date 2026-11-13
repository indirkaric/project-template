package {{rootPackage}}.security;

import {{rootPackage}}.constants.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates service-to-service calls by {@code Sqa-Client-Id}. Unknown ids get no authorities.
 */
public final class SqaClientIdHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getHeader(Constants.SQA_CLIENT_ID_HEADER);
        if (clientId != null) {
            List<SimpleGrantedAuthority> authorities = Constants.TRUSTED_CLIENTS.contains(clientId)
                    ? Constants.Authority.fullAuthorities()
                    : List.of();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(clientId, null, authorities));
        }

        filterChain.doFilter(request, response);
    }
}
