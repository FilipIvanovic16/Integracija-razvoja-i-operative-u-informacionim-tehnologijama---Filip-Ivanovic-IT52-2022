package com.chronoshop.event;

import java.time.LocalDateTime;

/**
 * Publikuje payment-service na razmenu "chronoshop.events" (routing key "payment.failed") kada
 * Stripe placanje ne uspe. Konzumiraju je order-service (menja status porudzbine u CANCELLED) i
 * notification-service (obavestenje korisniku o neuspelom placanju).
 */
public record PaymentFailedEvent(
    Long orderId, String orderNumber, String reason, LocalDateTime occurredAt) {
  public static PaymentFailedEvent of(Long orderId, String orderNumber, String reason) {
    return new PaymentFailedEvent(orderId, orderNumber, reason, LocalDateTime.now());
  }
}
