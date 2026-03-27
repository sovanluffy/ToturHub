package com.rental_api.ServiceBooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        // Essential: Define the production server for Swagger to use HTTPS
        Server prodServer = new Server()
                .url("https://toturhub-dev.onrender.com")
                .description("Production Server (Render)");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development");

        return new OpenAPI()
                .info(new Info()
                        .title("ServiceBooking API")
                        .version("1.0")
                        .description("API Documentation for ToturHub"))
                // Setting servers ensures the "Execute" button uses the correct base URL
                .servers(List.of(prodServer, localServer))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}