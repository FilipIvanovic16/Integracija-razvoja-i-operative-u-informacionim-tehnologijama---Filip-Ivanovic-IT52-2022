package com.chronoshop.catalog.config;

import com.chronoshop.catalog.security.GatewayHeaderAuthenticationFilter;
import com.chronoshop.catalog.security.RestAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;
    private final RestAuthEntryPoint authEntryPoint;

    public SecurityConfig(GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
                          RestAuthEntryPoint authEntryPoint) {
        this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/watches/**", "/api/brands/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/watches/*/stock").hasAnyRole("ADMIN", "SERVICE")
                        .requestMatchers(HttpMethod.POST, "/api/watches/**", "/api/brands/**", "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/watches/**", "/api/brands/**", "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/watches/**", "/api/brands/**", "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
