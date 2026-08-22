package com.chronoshop.payment.config;

import com.chronoshop.payment.security.GatewayHeaderAuthenticationFilter;
import com.chronoshop.payment.security.RestAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  public SecurityConfig(
      GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
      RestAuthEntryPoint authEntryPoint) {
    this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
    this.authEntryPoint = authEntryPoint;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(eh -> eh.authenticationEntryPoint(authEntryPoint))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/error")
                    .permitAll()
                    .requestMatchers(
                        "/actuator/health", "/actuator/health/**", "/actuator/prometheus")
                    .permitAll()
                    // Stripe zove webhook spolja, bez ikakvog zaglavlja identiteta -
                    // potpis se kriptografski verifikuje u PaymentService, ne ovde.
                    .requestMatchers("/api/payments/webhook")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
