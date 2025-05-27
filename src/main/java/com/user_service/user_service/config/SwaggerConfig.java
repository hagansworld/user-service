package com.user_service.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger OpenAPI documentation configuration for the User Service.
 * Includes JWT Bearer token authentication setup.
 *
 * @author Isaac Hagan
 * @since 23rd May 2025
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Create the security scheme for Bearer authentication.
     */
    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Provide the JWT token. Example: Bearer eyJhbGciOiJIUzI1...");
    }

    /**
     * Configure the OpenAPI with JWT Bearer Authentication and general API metadata.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createBearerScheme()))
                .info(new Info()
                        .title("USER SERVICE API")
                        .version("1.0")
                        .description("This service handles the management of all user registration, login, and profile operations.")
                        .contact(new Contact()
                                .name("Isaac Hagan")
                                .email("isaachagan320@gmail.com")
                                .url("https://isaacdev.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
