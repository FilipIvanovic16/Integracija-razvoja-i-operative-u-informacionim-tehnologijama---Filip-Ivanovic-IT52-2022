package com.chronoshop.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Publikuje order-service na razmenu "chronoshop.events" (routing key "order.created")
 * kada je porudzbina kreirana. Konzumira je payment-service radi inicijalizacije placanja.
 */
public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        String customerEmail,
        String customerName,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime occurredAt
) {
    public static OrderCreatedEvent of(Long orderId, String orderNumber, String customerEmail,
                                        String customerName, BigDecimal totalAmount, String currency) {
        return new OrderCreatedEvent(orderId, orderNumber, customerEmail, customerName,
                totalAmount, currency, LocalDateTime.now());
    }
}
