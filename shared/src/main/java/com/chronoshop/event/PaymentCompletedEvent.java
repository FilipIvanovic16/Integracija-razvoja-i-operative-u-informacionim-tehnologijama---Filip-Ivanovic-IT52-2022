package com.chronoshop.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Publikuje payment-service na razmenu "chronoshop.events" (routing key "payment.completed")
 * kada je Stripe placanje uspesno zavrseno. Konzumiraju je order-service (menja status
 * porudzbine u PAID) i notification-service (SSE obavestenje korisniku).
 */
public record PaymentCompletedEvent(
        Long orderId,
        String orderNumber,
        String stripePaymentIntentId,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt
) {
    public static PaymentCompletedEvent of(Long orderId, String orderNumber,
                                            String stripePaymentIntentId, BigDecimal amount, String currency) {
        return new PaymentCompletedEvent(orderId, orderNumber, stripePaymentIntentId,
                amount, currency, LocalDateTime.now());
    }
}
