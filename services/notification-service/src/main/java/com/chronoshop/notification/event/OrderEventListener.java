package com.chronoshop.notification.event;

import com.chronoshop.event.OrderCreatedEvent;
import com.chronoshop.notification.model.NotificationEvent;
import com.chronoshop.notification.stream.NotificationBroadcaster;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final NotificationBroadcaster broadcaster;

    public OrderEventListener(NotificationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @RabbitListener(queues = "notification-service.order.created")
    public void onOrderCreated(OrderCreatedEvent event) {
        broadcaster.publish(NotificationEvent.of(
                "ORDER_CREATED",
                "Porudžbina " + event.orderNumber() + " je kreirana.",
                event.orderNumber()));
    }
}
