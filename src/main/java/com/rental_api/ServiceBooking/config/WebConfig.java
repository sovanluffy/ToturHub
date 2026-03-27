package com.rental_api.ServiceBooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS to allow all origins, methods, and headers.
     * This is important if your front-end runs on a different domain or port.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOriginPatterns("*") // Allow all origins
                .allowedMethods("*")        // Allow GET, POST, PUT, DELETE, OPTIONS
                .allowedHeaders("*")        // Allow all headers
                .allowCredentials(true);    // Allow cookies or auth headers
    }

    /**
     * Serve uploaded files from the "uploads" folder in the project root.
     * Example: Access http://localhost:8080/uploads/image.png
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // Maps to local folder
    }
}