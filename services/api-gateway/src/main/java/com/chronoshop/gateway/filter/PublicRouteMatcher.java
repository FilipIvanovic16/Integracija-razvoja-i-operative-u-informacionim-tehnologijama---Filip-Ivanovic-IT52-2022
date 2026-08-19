package com.chronoshop.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Rute koje ne zahtevaju JWT - ogledaju permitAll pravila vec definisana u
 * SecurityConfig-u svakog pojedinacnog servisa (auth-service: /api/auth/**;
 * catalog-service: GET na watches/brands/categories/uploads; payment-service:
 * Stripe webhook). Gateway ovde odlucuje da li uopste trazi Authorization
 * zaglavlje pre nego sto zahtev prosledi dalje.
 */
@Component
public class PublicRouteMatcher {

    public boolean isPublic(String path, HttpMethod method) {
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (path.equals("/api/payments/webhook")) {
            return true;
        }
        if (path.startsWith("/actuator/")) {
            return true;
        }
        if (method == HttpMethod.GET && (
                path.startsWith("/api/watches") ||
                path.startsWith("/api/brands") ||
                path.startsWith("/api/categories") ||
                path.startsWith("/api/uploads"))) {
            return true;
        }
        return false;
    }
}
