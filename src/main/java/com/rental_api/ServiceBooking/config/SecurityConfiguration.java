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
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization
                .authorizeHttpRequests(auth -> auth

                        // Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/auth/**",
                                "/auth/**",
                                "/auth/google/**"
                        ).permitAll()

                        // Public resources
                        .requestMatchers(
                                "/api/v1/public/**",
                                "/uploads/**"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Public GET APIs
                        .requestMatchers(HttpMethod.GET, "/api/v1/tutors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/classes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/tutor-cards").permitAll()

                        // FIXED: your endpoint
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/public-cards").permitAll()

                        // or allow all open classes
                        .requestMatchers(HttpMethod.GET, "/api/v1/open-classes/**").permitAll()

                        // Admin
                        .requestMatchers("/api/v1/admin/**").hasRole("admin")
                        .requestMatchers("/api/categories/**").hasRole("admin")
                        .requestMatchers("/users/**").hasRole("admin")

                        // Tutor
                        .requestMatchers(HttpMethod.POST, "/api/v1/tutors/**").hasRole("tutor")
                        .requestMatchers(HttpMethod.POST, "/api/v1/classes/open").hasRole("tutor")

                        // Student
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").hasRole("student")

                        // Others require auth
                        .anyRequest().authenticated()
                )

                // Stateless JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Provider
                .authenticationProvider(authenticationProvider)

                // JWT Filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Exception handler
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }

    // Global CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // allow all http origins
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

