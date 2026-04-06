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
                // 1. CORS MUST BE FIRST in the chain
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Always allow Preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC: Auth & Public Resources
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/public/**",
                                "/api/v1/locations/**", // Fixed 401 for locations
                                "/auth/**",
                                "/uploads/**"
                        ).permitAll()

                        // PUBLIC: Swagger Documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // PUBLIC: Specific GET APIs for the Frontend
                        .requestMatchers(HttpMethod.GET, "/api/v1/tutors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/classes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/**").permitAll()

                        // ROLE-BASED: Admin (Ensure DB uses ROLE_ADMIN)
                        .requestMatchers("/api/v1/admin/**", "/users/**").hasRole("ADMIn")

                        // ROLE-BASED: Tutor (Ensure DB uses ROLE_TUTOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/tutors/**").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/classes/open").hasRole("TUTOR")

                        // ROLE-BASED: Student (Ensure DB uses ROLE_STUDENT)
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").hasRole("STUDENT")

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

        // ALLOW FRONTEND ORIGINS (No trailing slashes!)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173", // Vite
                "http://localhost:3000", // Standard React
                "http://localhost:5181",
                "https://toturhub-dev.onrender.com"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}