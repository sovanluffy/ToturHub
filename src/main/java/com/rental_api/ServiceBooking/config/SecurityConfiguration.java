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
                // ================= CORS =================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // ================= AUTHORIZATION =================
                .authorizeHttpRequests(auth -> auth

                        // ================= PRE-FLIGHT =================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ================= WEBSOCKET =================
                        .requestMatchers("/ws/**", "/ws").permitAll()

                        // ================= AUTH =================
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/auth/**",
                                "/auth/**",
                                "/auth/google/**"
                        ).permitAll()

                        // ================= SWAGGER =================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ================= PUBLIC STATIC =================
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/v1/locations/**").permitAll()
                        .requestMatchers("/api/subjects/**").permitAll()

                        // ================= OPEN CLASS PUBLIC APIs =================
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/tutor/**").permitAll()

                        // ================= REVIEWS (NEWLY ADDED) =================
                        // Allow anyone to see reviews
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/class/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/tutor/**").permitAll()
                        // Only students can write reviews
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/class/**").hasRole("STUDENT")

                        // ================= TUTOR (WRITE ONLY) =================
                        .requestMatchers(HttpMethod.POST, "/api/v1/open-classes/**").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/open-classes/**").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/open-classes/**").hasRole("TUTOR")

                        // ================= CHAT =================
                        .requestMatchers("/api/v1/chat/**").authenticated()

                        // ================= ADMIN =================
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // ================= TUTOR SPECIFIC =================
                        .requestMatchers(HttpMethod.POST, "/api/v1/tutors/**").hasRole("TUTOR")

                        // ================= BOOKING =================
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/book-class/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/me").authenticated() // Added for frontend sync
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/user/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/tutor/**").hasAnyRole("TUTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/class/**").hasAnyRole("TUTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/confirm/**").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/reject/**").hasRole("TUTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bookings/**").hasAnyRole("STUDENT", "ADMIN")

                        // ================= DEFAULT =================
                        .anyRequest().authenticated()
                )

                // ================= SESSION =================
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ================= PROVIDER =================
                .authenticationProvider(authenticationProvider)

                // ================= JWT FILTER =================
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ================= EXCEPTION =================
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }

    // ================= CORS CONFIG =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}