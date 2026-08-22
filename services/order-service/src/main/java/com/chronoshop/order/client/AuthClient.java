package com.chronoshop.order.client;

import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * REST klijent ka auth-service - "order-service -> auth-service (validacija korisnika)" iz
 * specifikacije komunikacije. Nema gRPC/MQ ekvivalent, ostaje REST trajno.
 */
@Component
public class AuthClient {

  private final RestClient restClient;

  public AuthClient(RestClient.Builder builder, @Value("${app.auth-service.url}") String baseUrl) {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  public UserResponse getUser(Long userId) {
    try {
      return restClient.get().uri("/api/users/{id}", userId).retrieve().body(UserResponse.class);
    } catch (RestClientResponseException e) {
      throw translate(e, "Korisnik", userId);
    }
  }

  public AddressResponse getAddress(Long userId, Long addressId) {
    try {
      return restClient
          .get()
          .uri("/api/users/{userId}/addresses/{addressId}", userId, addressId)
          .retrieve()
          .body(AddressResponse.class);
    } catch (RestClientResponseException e) {
      throw translate(e, "Adresa", addressId);
    }
  }

  private RuntimeException translate(RestClientResponseException e, String resource, Object id) {
    if (e.getStatusCode().value() == 404) {
      return new ResourceNotFoundException(resource, id);
    }
    return e;
  }
}
