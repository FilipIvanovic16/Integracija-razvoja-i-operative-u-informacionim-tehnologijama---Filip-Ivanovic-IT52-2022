package com.chronoshop.gateway.filter;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Svaki zahtev dobija X-Correlation-Id (preuzet ako vec postoji, inace generisan) - prosledjuje se
 * downstream servisima i vraca u odgovoru, radi povezivanja logova/ trace-ova kroz ceo lanac poziva
 * (feat/observability ce ga citati u MDC-u).
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

  public static final String HEADER = "X-Correlation-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    ServerHttpRequest mutatedRequest =
        exchange.getRequest().mutate().header(HEADER, correlationId).build();
    exchange.getResponse().getHeaders().add(HEADER, correlationId);

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  @Override
  public int getOrder() {
    // Pre JWT filtera - korelacioni ID treba da postoji i na 401 odgovorima.
    return -2;
  }
}
