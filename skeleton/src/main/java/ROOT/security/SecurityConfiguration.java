package {{rootPackage}}.security;

import {{rootPackage}}.service.security.IScopeContext;
import {{rootPackage}}.util.Check;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Filter chain: ClientId, then RequestId, then SqaUserId, then Keycloak JWT.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    private static final String[] AUTH_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/actuator/health/**"
    };

    /**
     * Designed for extension.
     *
     * @param http security builder
     * @param scopeContext scope context shared by the filters
     * @return the filter chain
     * @throws Exception on misconfiguration
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http, final IScopeContext scopeContext)
            throws Exception {
        Check.isNotNull(http, "http");
        Check.isNotNull(scopeContext, "scopeContext");

        // The filters are not Spring beans on purpose: as beans, Boot would also register them
        // as plain servlet filters and every request would pass through them twice.
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new SqaClientIdHeaderAuthenticationFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new SqaRequestIdHeaderFilter(scopeContext), BasicAuthenticationFilter.class)
                .addFilterBefore(new SqaUserIdAuthFilter(scopeContext), BasicAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .exceptionHandling(handling -> handling.accessDeniedHandler(new CustomOAuth2AccessDeniedHandler()))
                .build();
    }
}
