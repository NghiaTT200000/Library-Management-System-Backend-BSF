package com.library_management.library_management_artifact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    private final AppProperties appProperties;

    @Bean
    public OpenAPI openAPI() {
        AppProperties.Swagger swagger = appProperties.getSwagger();
        return new OpenAPI()
                .info(new Info()
                        .title(swagger.getTitle())
                        .version(swagger.getVersion())
                        .description(swagger.getDescription()));
    }
}
