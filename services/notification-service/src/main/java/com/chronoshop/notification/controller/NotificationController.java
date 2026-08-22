package com.chronoshop.notification.controller;

import com.chronoshop.notification.model.NotificationEvent;
import com.chronoshop.notification.stream.NotificationRxAdapter;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

  private final NotificationRxAdapter rxAdapter;

  public NotificationController(NotificationRxAdapter rxAdapter) {
    this.rxAdapter = rxAdapter;
  }

  /**
   * Broadcast SSE tok - svi trenutno povezani klijenti dobijaju iste dogadjaje (order.created,
   * payment.completed, payment.failed), bez filtriranja po korisniku.
   */
  @GetMapping(value = "/api/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flowable<ServerSentEvent<NotificationEvent>> stream() {
    return rxAdapter.stream()
        .map(event -> ServerSentEvent.builder(event).event(event.type()).build());
  }
}
