package com.chronoshop.order.client;

import com.chronoshop.dto.CatalogDtos.StockAdjustmentRequest;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST klijent ka catalog-service - proverava/rezerviše zalihe pre kreiranja porudžbine.
 * Zameniće ga gRPC CheckStock/ReserveStock (feat/grpc-stock-check); ova REST putanja
 * ostaje kao fallback pri nedostupnosti gRPC servera, po specifikaciji.
 * Poziv rezervacije se identifikuje kao "SERVICE" (X-User-Roles), ne kao konkretan korisnik.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(RestClient.Builder builder, @Value("${app.catalog-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public WatchResponse getWatch(Long watchId) {
        try {
            return restClient.get()
                    .uri("/api/watches/{id}", watchId)
                    .retrieve()
                    .body(WatchResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Sat", watchId);
            }
            throw e;
        }
    }

    /** delta &lt; 0 rezerviše (umanjuje) zalihe, delta &gt; 0 ih vraća (otkazana porudžbina). */
    public void adjustStock(Long watchId, int delta) {
        restClient.patch()
                .uri("/api/watches/{id}/stock", watchId)
                .header("X-User-Id", "order-service")
                .header("X-User-Roles", "SERVICE")
                .body(new StockAdjustmentRequest(delta))
                .retrieve()
                .toBodilessEntity();
    }
}
