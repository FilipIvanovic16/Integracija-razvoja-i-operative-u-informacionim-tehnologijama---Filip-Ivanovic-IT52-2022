package com.chronoshop.payment.client;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.OrderDtos.UpdateOrderStatusRequest;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST klijent ka order-service. Zameniće ga (za tok narudžbina -> naplata) RabbitMQ
 * događaji order.created / payment.completed / payment.failed (feat/rabbitmq-events);
 * dok ta grana ne postoji, plaćanje se inicira i status ažurira sinhrono preko REST-a.
 */
@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(RestClient.Builder builder, @Value("${app.order-service.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Cita porudzbinu u ime konkretnog korisnika - prosledjuje njegov identitet dalje,
     * pa proveru vlasnistva/admin prava radi order-service (isto kao da je gateway
     * validirao token).
     */
    public OrderResponse getOrder(Long orderId, Long userId, boolean isAdmin) {
        try {
            return restClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Roles", isAdmin ? "ADMIN" : "CUSTOMER")
                    .retrieve()
                    .body(OrderResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Porudžbina", orderId);
            }
            throw e;
        }
    }

    /**
     * Azuriranje statusa posle Stripe webhook-a - nema kontekst ulogovanog korisnika
     * (poziva ga Stripe), pa se poziv samo-oznacava kao SERVICE.
     */
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        restClient.put()
                .uri("/api/admin/orders/{id}/status", orderId)
                .header("X-User-Id", "payment-service")
                .header("X-User-Roles", "SERVICE")
                .body(new UpdateOrderStatusRequest(status))
                .retrieve()
                .toBodilessEntity();
    }
}
