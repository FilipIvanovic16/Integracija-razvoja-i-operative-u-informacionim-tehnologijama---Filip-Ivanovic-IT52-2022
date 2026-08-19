package com.chronoshop.gateway.filter;

import com.chronoshop.gateway.error.ApiErrorBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Jedino mesto u sistemu koje validira JWT (nasuprot dosadasnjeg gateway-header
 * "trust" obrasca u catalog/order/payment servisima). Validan token se pretvara u
 * X-User-Id / X-User-Roles zaglavlja koje ti servisi vec citaju - njihove GatewayHeader
 * filtere ne treba menjati, samo od sada tim zaglavljima zaista prethodi provera potpisa.
 */
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final SecretKey key;
    private final PublicRouteMatcher publicRouteMatcher;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public JwtAuthenticationGlobalFilter(@Value("${app.jwt.secret}") String secret,
                                         PublicRouteMatcher publicRouteMatcher) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.publicRouteMatcher = publicRouteMatcher;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (publicRouteMatcher.isPublic(path, request.getMethod())) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(exchange, "Nedostaje autorizacioni token.");
        }

        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();

            Object userId = claims.get("uid");
            String role = claims.get("role", String.class);
            if (userId == null || role == null) {
                return unauthorized(exchange, "Token ne sadrži očekivane podatke.");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Roles", role)
                    .header("X-User-Email", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Nevalidan ili istekao token.");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiErrorBody error = ApiErrorBody.of(401, "Unauthorized", message,
                exchange.getRequest().getURI().getPath());
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(error);
        } catch (Exception e) {
            bytes = ("{\"error\":\"Unauthorized\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Posle korelacionog ID-a, pre rutiranja ka backend servisima.
        return -1;
    }
}
