package com.chronoshop.order.event;

import static org.mockito.Mockito.verify;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.event.PaymentCompletedEvent;
import com.chronoshop.event.PaymentFailedEvent;
import com.chronoshop.order.service.OrderService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

  @Mock private OrderService orderService;

  @Test
  void onPaymentCompleted_updatesOrderStatusToPaid() {
    PaymentCompletedEvent event =
        PaymentCompletedEvent.of(42L, "ORD-1", "pi_123", new BigDecimal("100.00"), "EUR");

    new PaymentEventListener(orderService).onPaymentCompleted(event);

    verify(orderService).updateStatus(42L, OrderStatus.PAID);
  }

  @Test
  void onPaymentFailed_updatesOrderStatusToCancelled() {
    PaymentFailedEvent event = PaymentFailedEvent.of(42L, "ORD-1", "card declined");

    new PaymentEventListener(orderService).onPaymentFailed(event);

    verify(orderService).updateStatus(42L, OrderStatus.CANCELLED);
  }
}
