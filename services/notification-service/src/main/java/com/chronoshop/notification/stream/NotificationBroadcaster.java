package com.chronoshop.notification.stream;

import com.chronoshop.notification.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Sinks.Many broadcast tacka - jedan tok koji svi trenutno povezani SSE klijenti dele
 * (nema perzistencije/istorije, notification-service nema bazu). RabbitMQ listeneri
 * (OrderEventListener/PaymentEventListener) su jedini koji ovde pisu; SSE kontroler
 * (kroz NotificationRxAdapter) samo cita.
 */
@Component
public class NotificationBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NotificationBroadcaster.class);

    private final Sinks.Many<NotificationEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

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
