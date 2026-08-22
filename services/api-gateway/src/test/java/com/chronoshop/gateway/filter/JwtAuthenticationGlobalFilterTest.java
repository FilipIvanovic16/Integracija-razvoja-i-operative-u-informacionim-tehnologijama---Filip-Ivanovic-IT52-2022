package com.chronoshop.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationGlobalFilterTest {

  private static final String SECRET =
      "Z2VuZXJpc2FuLXRham5pLWtsanVjLXphLWNocm9ub3Nob3AtMjAyNi1lb25pcw==";

  private final JwtAuthenticationGlobalFilter filter =
      new JwtAuthenticationGlobalFilter(SECRET, new PublicRouteMatcher());

  @Test
  void publicRoute_passesThroughWithoutToken() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/watches"));
    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    assertThat(exchange.getResponse().getStatusCode()).isNull();
  }

  @Test
  void protectedRoute_withoutToken_returns401() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders"));
    GatewayFilterChain chain = mock(GatewayFilterChain.class);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(chain, never()).filter(any());
  }

  @Test
  void protectedRoute_withInvalidToken_returns401() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/orders")
                .header("Authorization", "Bearer garbage-token"));
    GatewayFilterChain chain = mock(GatewayFilterChain.class);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(chain, never()).filter(any());
  }

  @Test
  void protectedRoute_withValidToken_forwardsUserHeaders() {
    String token = validToken(7L, "ADMIN", "admin@chronoshop.rs");
    ServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/admin/orders")
                .header("Authorization", "Bearer " + token));
    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain)
        .filter(
            argThat(
                mutated ->
                    "7".equals(mutated.getRequest().getHeaders().getFirst("X-User-Id"))
                        && "ADMIN"
                            .equals(mutated.getRequest().getHeaders().getFirst("X-User-Roles"))
                        && "admin@chronoshop.rs"
                            .equals(mutated.getRequest().getHeaders().getFirst("X-User-Email"))));
  }

  @Test
  void protectedRoute_withExpiredToken_returns401() throws InterruptedException {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    Date now = new Date();
    String expired =
        Jwts.builder()
            .subject("kupac@chronoshop.rs")
            .claim("uid", 1L)
            .claim("role", "CUSTOMER")
            .issuedAt(now)
            .expiration(new Date(now.getTime() + 1))
            .signWith(key)
            .compact();
    Thread.sleep(20);

    ServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/orders").header("Authorization", "Bearer " + expired));
    GatewayFilterChain chain = mock(GatewayFilterChain.class);

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  private String validToken(Long userId, String role, String email) {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    Date now = new Date();
    return Jwts.builder()
        .subject(email)
        .claim("uid", userId)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + 60_000))
        .signWith(key)
        .compact();
  }
}
