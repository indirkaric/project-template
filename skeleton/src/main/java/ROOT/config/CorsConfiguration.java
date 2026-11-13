package {{rootPackage}}.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS, same as the other platform services.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    /**
     * Designed for extension.
     *
     * @param registry registry
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
    }
}
