package com.chronoshop.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * RequestRateLimiter (Redis-backed) ogranicava po IP adresi klijenta - jednostavno
     * i radi i za javne rute (npr. GET /api/watches) gde jos nema X-User-Id.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }
}
