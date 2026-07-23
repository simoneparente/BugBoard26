package it.unina.bugboard.bugboard_backend.config;

import it.unina.bugboard.bugboard_backend.security.CustomAccessDeniedHandler;
import it.unina.bugboard.bugboard_backend.security.JwtAuthenticationEntryPoint;
import it.unina.bugboard.bugboard_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String corsAllowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // CSRF protection is intentionally disabled. Even if we use cookie-based
        // authentication,
        // CSRF defense is enforced by the 'SameSite=Strict' attribute configured on the
        // HttpOnly
        // JWT cookie. This tells the browser to never attach the authentication cookie
        // to
        // cross-site requests, preventing CSRF attacks.
        @SuppressWarnings("squid:S4502")
        HttpSecurity configuredHttp = http.csrf(AbstractHttpConfigurer::disable);

        configuredHttp
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/users/register", "/api/auth/logout").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/admin/**", "/api/invitations/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/me").authenticated()
                        // Modalità Read-Only per utenti EXTERNAL:
                        // Solo ADMIN e TECHNICAL possono effettuare chiamate di modifica (POST, PUT, PATCH, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "TECHNICAL")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "TECHNICAL")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "TECHNICAL")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("ADMIN", "TECHNICAL")
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return configuredHttp.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse allowed origins from configuration (comma-separated)
        List<String> allowedOrigins = Arrays.asList(corsAllowedOrigins.split(","));
        allowedOrigins = allowedOrigins.stream()
                .map(String::trim)
                .toList();
        
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", config);
        return src;
    }

}
