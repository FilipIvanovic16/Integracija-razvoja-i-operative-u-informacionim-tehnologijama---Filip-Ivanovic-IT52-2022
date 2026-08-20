package com.chronoshop.notification.event;

import com.chronoshop.event.PaymentCompletedEvent;
import com.chronoshop.event.PaymentFailedEvent;
import com.chronoshop.notification.model.NotificationEvent;
import com.chronoshop.notification.stream.NotificationBroadcaster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private NotificationBroadcaster broadcaster;

    @Test
    void onPaymentCompleted_publishesNotification() {
        PaymentCompletedEvent event = PaymentCompletedEvent.of(1L, "ORD-1", "pi_123", new BigDecimal("100.00"), "EUR");

        new PaymentEventListener(broadcaster).onPaymentCompleted(event);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(broadcaster).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("PAYMENT_COMPLETED");
    }

    @Test
    void onPaymentFailed_publishesNotification() {
        PaymentFailedEvent event = PaymentFailedEvent.of(1L, "ORD-1", "card declined");

        new PaymentEventListener(broadcaster).onPaymentFailed(event);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(broadcaster).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("PAYMENT_FAILED");
    }
}
