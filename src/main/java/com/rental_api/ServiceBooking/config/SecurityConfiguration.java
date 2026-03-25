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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // OPTIONS preflight for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ Public endpoints: register & login
                .requestMatchers(
                        "/api/auth/**",
                        "/auth/**",
                        "/auth/google/**",
                        "/api/v1/auth-service/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/provider-requests/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/provider-requests/*/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/all").hasRole("ADMIN")

                // Customer endpoints
                .requestMatchers(HttpMethod.POST, "/api/services/*/bookings").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/my").hasRole("CUSTOMER")

                // Provider endpoints
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/accept").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/reject").hasRole("PROVIDER")

                // Authenticated for all other endpoints
                .requestMatchers("/api/services/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/provider-requests/request").authenticated()

                // Catch-all
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            // JWT filter must be after permitAll checks
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            );

        return http.build();
    }

    // ------------------- CORS -------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:42239"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Content-Type","X-Requested-With","Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}