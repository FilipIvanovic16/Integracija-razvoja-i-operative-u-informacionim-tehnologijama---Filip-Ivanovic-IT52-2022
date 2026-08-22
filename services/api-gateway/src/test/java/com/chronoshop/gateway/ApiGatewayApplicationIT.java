package com.chronoshop.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiGatewayApplicationIT {

  @Container
  @ServiceConnection("redis")
  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  @Autowired private WebTestClient webTestClient;

  @Test
  void protectedRoute_withoutToken_returns401() {
    webTestClient.get().uri("/api/orders").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void authRoute_isPublic_andReachesBackendAttempt() {
    webTestClient
        .post()
        .uri("/api/auth/login")
        .exchange()
        .expectStatus()
        .value(status -> org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(401));
  }
}
