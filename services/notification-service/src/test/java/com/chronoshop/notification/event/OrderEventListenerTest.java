package com.chronoshop.notification.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.chronoshop.event.OrderCreatedEvent;
import com.chronoshop.notification.model.NotificationEvent;
import com.chronoshop.notification.stream.NotificationBroadcaster;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

  @Mock private NotificationBroadcaster broadcaster;

  @Test
  void onOrderCreated_publishesNotificationWithOrderNumber() {
    OrderCreatedEvent event =
        OrderCreatedEvent.of(
            1L, "ORD-1", "kupac@chronoshop.rs", "Petar Petrović", new BigDecimal("100.00"), "EUR");

    new OrderEventListener(broadcaster).onOrderCreated(event);

    ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
    verify(broadcaster).publish(captor.capture());
    assertThat(captor.getValue().type()).isEqualTo("ORDER_CREATED");
    assertThat(captor.getValue().orderNumber()).isEqualTo("ORD-1");
  }
}
