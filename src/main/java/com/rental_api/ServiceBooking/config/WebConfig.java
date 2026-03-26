package com.rental_api.ServiceBooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins(
                        "https://toturhub-dev.onrender.com", // Your Live URL
                        "http://localhost:8080",            // Local Dev
                        "http://127.0.0.1:5500"             // Live Server/Frontend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Keeps your existing file upload path working
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}