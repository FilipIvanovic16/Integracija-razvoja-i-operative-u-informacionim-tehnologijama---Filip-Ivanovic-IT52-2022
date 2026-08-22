package com.chronoshop.notification.stream;

import com.chronoshop.notification.model.NotificationEvent;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class NotificationBroadcasterTest {

  @Test
  void publish_deliversEventToActiveSubscriber() {
    NotificationBroadcaster broadcaster = new NotificationBroadcaster();
    NotificationEvent event =
        NotificationEvent.of("ORDER_CREATED", "Porudžbina ORD-1 je kreirana.", "ORD-1");

    StepVerifier.create(broadcaster.stream().take(1))
        .then(() -> broadcaster.publish(event))
        .expectNext(event)
        .verifyComplete();
  }

  @Test
  void publish_withNoSubscribers_doesNotThrow() {
    NotificationBroadcaster broadcaster = new NotificationBroadcaster();

    broadcaster.publish(NotificationEvent.of("PAYMENT_FAILED", "Neuspelo.", "ORD-3"));
    // Nema izuzetka - onBackpressureBuffer prihvata emitovanje i bez pretplatnika.
  }
}
