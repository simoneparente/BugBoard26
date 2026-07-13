package it.unina.bugboard.bugboard_backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the global OpenAPI security scheme for JWT cookie-based authentication.
 * <p>
 * The scheme is defined here (rather than inline on each controller) so it is registered
 * once and reused across the entire API surface. To protect an endpoint, annotate the
 * controller method or class with:
 * <pre>
 *   {@code @SecurityRequirement(name = "cookieAuth")}
 * </pre>
 */
@Configuration
@SecurityScheme(
        name = "cookieAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "bugboard_jwt",
        description = "JWT Token in an HttpOnly Cookie for authentication"
)
public class OpenApiConfig {
    
}