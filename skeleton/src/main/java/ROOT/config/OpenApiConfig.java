package {{rootPackage}}.config;

import {{rootPackage}}.constants.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI definition with the bearer scheme every endpoint references.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Designed for extension.
     *
     * @return OpenAPI definition
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("{{service-name}} API").version("1.0")
                        .description("{{ServiceName}} microservice API."))
                .components(new Components().addSecuritySchemes(Constants.BEARER_KEY_VALUE,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme(Constants.SECURITY_SCHEME).bearerFormat(Constants.SECURITY_FORMAT)));
    }
}
