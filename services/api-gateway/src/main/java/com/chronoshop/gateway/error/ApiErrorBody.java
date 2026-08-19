package com.chronoshop.gateway.error;

import java.time.LocalDateTime;

/**
 * Isti oblik JSON greske kao shared ApiError iz servisa iza gateway-a (namerno
 * dupliran, ne uvozen - vidi napomenu u pom.xml o WebFlux/servlet konfliktu).
 */
public record ApiErrorBody(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiErrorBody of(int status, String error, String message, String path) {
        return new ApiErrorBody(LocalDateTime.now(), status, error, message, path);
    }
}
