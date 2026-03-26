package com.rental_api.ServiceBooking.config;

import com.rental_api.ServiceBooking.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 1. Apply CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow Preflight OPTIONS requests for all paths
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints & Swagger
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/auth/**",
                        "/auth/google/**",
                        "/api/v1/public/**",
                        "/api/v1/auth-service/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/uploads/**" // Allow access to uploaded files
                ).permitAll()

                // Public GET access
                .requestMatchers(HttpMethod.GET, "/api/v1/tutors/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/classes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/public/tutor-cards").permitAll()

                // Admin Access
                .requestMatchers("/api/v1/admin/**").hasRole("admin")
                .requestMatchers("/api/categories/**").hasRole("admin")
                .requestMatchers("/users/**").hasRole("admin")

                // Tutor/Student Specifics
                .requestMatchers(HttpMethod.POST, "/api/v1/tutors/**").hasRole("tutor")
                .requestMatchers(HttpMethod.POST, "/api/v1/classes/open").hasRole("tutor")
                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").hasRole("student")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ADD YOUR PRODUCTION URL HERE
        configuration.setAllowedOrigins(Arrays.asList(
            "https://toturhub-dev.onrender.com", // Essential for live Swagger
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:8080"
        ));
        
        // Allow all common headers and methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Simpler to allow all for Swagger
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}