package com.chronoshop.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.chronoshop.event.EventRouting;
import com.chronoshop.event.OrderCreatedEvent;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "20s")
@Testcontainers
class NotificationServiceApplicationIT {

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

  @Autowired private WebTestClient webTestClient;

  @Autowired private RabbitTemplate rabbitTemplate;

  @Test
  void orderCreatedMessage_isBroadcastOverSse() throws InterruptedException {
    OrderCreatedEvent event =
        OrderCreatedEvent.of(
            1L,
            "ORD-TEST-1",
            "kupac@chronoshop.rs",
            "Petar Petrović",
            new BigDecimal("100.00"),
            "EUR");

    Thread publisher =
        new Thread(
            () -> {
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }
              rabbitTemplate.convertAndSend(
                  EventRouting.EXCHANGE, EventRouting.ORDER_CREATED, event);
            });
    publisher.start();

    Flux<String> sseBody =
        webTestClient
            .get()
            .uri("/api/notifications/stream")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseBody();

    String received =
        sseBody.filter(chunk -> chunk.contains("ORD-TEST-1")).blockFirst(Duration.ofSeconds(15));

    publisher.join();
    assertThat(received).contains("ORDER_CREATED").contains("ORD-TEST-1");
  }
}
