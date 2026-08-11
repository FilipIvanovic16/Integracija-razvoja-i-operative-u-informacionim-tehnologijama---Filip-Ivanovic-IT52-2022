package com.chronoshop.dto;

import com.chronoshop.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreatePaymentIntentRequest(
            @NotNull Long orderId
    ) {
    }

    /** Vraća se frontendu da inicijalizuje Stripe plaćanje. */
    public record CheckoutSessionResponse(
            String clientSecret,
            String paymentIntentId,
            String orderNumber,
            BigDecimal amount,
            String currency,
            String publishableKeyHint
    ) {
    }

    /** Prikaz transakcije u admin panelu (na osnovu webhook podataka). */
    public record PaymentResponse(
            Long id,
            String orderNumber,
            String stripePaymentIntentId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String customerEmail,
            String customerName,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {
    }
}
