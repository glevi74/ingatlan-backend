package hu.ingatlan.rest;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ApplicationPath;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@ApplicationPath("/")
@OpenAPIDefinition(
    info = @Info(
        title = "Ingatlanközvetítő API",
        version = "1.0.0",
        description = "Ingatlanközvetítő szoftver REST API"
    ),
    security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
    securitySchemeName = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig extends Application {
}
