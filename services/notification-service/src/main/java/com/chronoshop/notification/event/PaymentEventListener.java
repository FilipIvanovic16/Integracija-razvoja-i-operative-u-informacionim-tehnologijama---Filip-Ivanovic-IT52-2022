package com.chronoshop.notification.event;

import com.chronoshop.event.PaymentCompletedEvent;
import com.chronoshop.event.PaymentFailedEvent;
import com.chronoshop.notification.model.NotificationEvent;
import com.chronoshop.notification.stream.NotificationBroadcaster;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final NotificationBroadcaster broadcaster;

    public PaymentEventListener(NotificationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @RabbitListener(queues = "notification-service.payment.completed")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        broadcaster.publish(NotificationEvent.of(
                "PAYMENT_COMPLETED",
                "Plaćanje za porudžbinu " + event.orderNumber() + " je uspešno.",
                event.orderNumber()));
    }

    @RabbitListener(queues = "notification-service.payment.failed")
    public void onPaymentFailed(PaymentFailedEvent event) {
        broadcaster.publish(NotificationEvent.of(
                "PAYMENT_FAILED",
                "Plaćanje za porudžbinu " + event.orderNumber() + " nije uspelo.",
                event.orderNumber()));
    }
}
