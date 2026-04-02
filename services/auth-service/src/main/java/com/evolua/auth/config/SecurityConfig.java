package com.evolua.auth.config;

import com.evolua.auth.infrastructure.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  private final String allowedOriginPatternsProperty;

  public SecurityConfig(
      @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
          String allowedOriginPatternsProperty) {
    this.allowedOriginPatternsProperty = allowedOriginPatternsProperty;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/v1/public/auth/register",
                        "/v1/public/auth/login",
                        "/v1/public/auth/refresh",
                        "/v1/public/auth/google/exchange")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/v1/public/auth/google/start",
                        "/auth/google/callback")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/v1/public/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var configuration = new CorsConfiguration();
    configuration.setAllowCredentials(true);
    configuration.setAllowedOriginPatterns(
        Arrays.stream(allowedOriginPatternsProperty.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList());
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
