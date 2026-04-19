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
                                // ✅ CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)

                                // ✅ AUTHORIZATION
                                .authorizeHttpRequests(auth -> auth

                                                // ✅ Allow preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // 🔥 VERY IMPORTANT (WebSocket)
                                                .requestMatchers("/ws/**").permitAll()

                                                // ✅ PUBLIC APIs
                                                .requestMatchers(HttpMethod.GET, "/api/v1/locations/**").permitAll()

                                                .requestMatchers(
                                                                "/api/v1/auth/**",
                                                                "/api/auth/**",
                                                                "/auth/**",
                                                                "/auth/google/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/v1/public/**",
                                                                "/uploads/**")
                                                .permitAll()

                                                // Swagger
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**",
                                                                "/swagger-resources/**",
                                                                "/webjars/**")
                                                .permitAll()

                                                // Public GET APIs
                                                .requestMatchers(HttpMethod.GET, "/api/v1/tutors/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/classes/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/public/tutor-cards")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/**").permitAll()

                                                // ================= ADMIN =================
                                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                                                .requestMatchers("/users/**").hasRole("ADMIN")

                                                // ================= TUTOR =================
                                                .requestMatchers(HttpMethod.POST, "/api/v1/tutors/**").hasRole("TUTOR")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/classes/open")
                                                .hasRole("TUTOR")

                                                // ================= BOOKING =================

                                                // CREATE
                                                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/book-class/**")
                                                .hasRole("STUDENT")

                                                // READ
                                                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/user/**")
                                                .hasAnyRole("STUDENT", "ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/tutor/**")
                                                .hasAnyRole("TUTOR", "ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/class/**")
                                                .hasAnyRole("TUTOR", "ADMIN")

                                                // UPDATE
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/confirm/**")
                                                .hasRole("TUTOR")

                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/reject/**")
                                                .hasRole("TUTOR")

                                                .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/**")
                                                .hasAnyRole("STUDENT", "ADMIN")

                                                // DELETE
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/bookings/**")
                                                .hasAnyRole("STUDENT", "ADMIN")

                                                // fallback
                                                .requestMatchers("/api/v1/bookings/**").authenticated()

                                                // everything else
                                                .anyRequest().authenticated())

                                // ✅ STATELESS
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ✅ PROVIDER
                                .authenticationProvider(authenticationProvider)

                                // ✅ JWT FILTER
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                                // ✅ EXCEPTION HANDLING
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler));

                return http.build();
        }

        // ================= CORS =================
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "http://localhost:5181",
                                "http://localhost:8080",
                                "https://toturhub-dev.onrender.com"));

                configuration.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "PATCH",
                                "OPTIONS"));

                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}