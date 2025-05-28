package com.user_service.user_service.security;

import com.user_service.user_service.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     *
     * This array is a list of URLs that should not be blocked by Spring Security.
     * It makes sure things like Swagger UI, static files, and actuator endpoints
     * stay publicly accessible.
     */

//    private static final String[] SWAGGER_ENDPOINTS = {
//            "/v3/api-docs/**",
//            "/swagger-ui/**",
//            "/swagger-ui.html",
//            "/swagger-resources/**",
//            "/configuration/**",
//            "/webjars/**",
//            "/actuator/**"
//    };



    /**
     * Configures the security filter chain for the application.
     *
     * @param http The HttpSecurity object used to configure the security rules.
     * @return A SecurityFilterChain object representing the configured security rules.
     * @throws Exception If an error occurs during configuration.
     */


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors((AbstractHttpConfigurer::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()
//                        .requestMatchers( "/users/**").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.USER.name())
                        .requestMatchers("/users/**").hasAuthority(UserRole.ADMIN.name())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8082")); // Dev origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // OPTIONS for preflight
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Common JWT headers
        configuration.setAllowCredentials(true); // If using cookies or credentials
        configuration.setExposedHeaders(List.of("Authorization")); // Optional: if you return the JWT in response headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
