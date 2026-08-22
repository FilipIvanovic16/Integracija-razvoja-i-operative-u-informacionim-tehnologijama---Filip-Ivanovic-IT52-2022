package com.chronoshop.notification.stream;

import com.chronoshop.notification.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

/**
 * Sinks.Many broadcast tacka - jedan tok koji svi trenutno povezani SSE klijenti dele (nema
 * perzistencije/istorije, notification-service nema bazu). RabbitMQ listeneri
 * (OrderEventListener/PaymentEventListener) su jedini koji ovde pisu; SSE kontroler (kroz
 * NotificationRxAdapter) samo cita. autoCancel=false je namerno: podrazumevano (true) bi trajno
 * ugasilo sink čim broj pretplatnika padne na nulu (npr. svi klijenti trenutno zatvore tab), posle
 * čega nijedna buduća notifikacija ne bi mogla da se emituje do restarta servisa.
 */
@Component
public class NotificationBroadcaster {

  private static final Logger log = LoggerFactory.getLogger(NotificationBroadcaster.class);

  private final Sinks.Many<NotificationEvent> sink =
      Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

  public void publish(NotificationEvent event) {
    Sinks.EmitResult result = sink.tryEmitNext(event);
    if (result.isFailure()) {
      log.warn("Neuspelo emitovanje notifikacije {}: {}", event.type(), result);
    }
  }

  public Flux<NotificationEvent> stream() {
    return sink.asFlux();
  }
}
