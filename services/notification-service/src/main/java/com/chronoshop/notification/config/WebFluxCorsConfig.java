package com.chronoshop.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * CORS za slucaj direktnog pristupa (lokalni razvoj bez gateway-a) - gateway (PR feat/api-gateway)
 * ima sopstvenu globalnu CORS konfiguraciju za saobracaj kroz njega.
 */
@Configuration
public class WebFluxCorsConfig implements WebFluxConfigurer {

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOriginPatterns(allowedOrigins.split(","))
        .allowedMethods("GET", "OPTIONS")
        .allowCredentials(true);
  }
}
