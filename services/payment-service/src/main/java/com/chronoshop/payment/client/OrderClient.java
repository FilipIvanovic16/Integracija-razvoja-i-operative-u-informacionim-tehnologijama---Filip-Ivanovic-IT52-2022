package com.chronoshop.payment.client;

import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST klijent ka order-service - i dalje jedini nacin da payment-service sinhrono pročita
 * porudžbinu (vlasništvo/iznos) pri pokretanju checkout-a. Javljanje novog statusa nazad
 * order-service-u vise ne ide ovim putem - to je RabbitMQ (vidi PaymentEventPublisher), po
 * specifikaciji.
 */
@Component
public class OrderClient {

  private final RestClient restClient;

  public OrderClient(
      RestClient.Builder builder, @Value("${app.order-service.url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  /**
   * Cita porudzbinu u ime konkretnog korisnika - prosledjuje njegov identitet dalje, pa proveru
   * vlasnistva/admin prava radi order-service (isto kao da je gateway validirao token).
   */
  public OrderResponse getOrder(Long orderId, Long userId, boolean isAdmin) {
    try {
      return restClient
          .get()
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
}
