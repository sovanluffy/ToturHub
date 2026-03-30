package com.rental_api.ServiceBooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all endpoints
                        .allowedOriginPatterns(
                            "http://localhost:3000",                // frontend dev
                            "http://localhost:8080",                // local backend or other service
                            "https://toturhub-dev.onrender.com"     // deployed frontend
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")          // allow all headers
                        .allowCredentials(true)       // allow cookies/session
                        .exposedHeaders("Authorization", "Content-Disposition"); // optional
            }
        };
    }
}