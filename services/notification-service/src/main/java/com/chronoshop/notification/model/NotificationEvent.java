package com.chronoshop.notification.model;

import java.time.LocalDateTime;

/**
 * Oblik poruke koja ide preko SSE ka frontend-u - namerno odvojen od internih
 * RabbitMQ event klasa (OrderCreatedEvent i sl. iz shared) jer je ovo javni,
 * spoljni ugovor prema pretraživaču, ne interni ugovor izmedju servisa.
 */
public record NotificationEvent(
        String type,
        String message,
        String orderNumber,
        LocalDateTime occurredAt
) {
    public static NotificationEvent of(String type, String message, String orderNumber) {
        return new NotificationEvent(type, message, orderNumber, LocalDateTime.now());
    }
}
