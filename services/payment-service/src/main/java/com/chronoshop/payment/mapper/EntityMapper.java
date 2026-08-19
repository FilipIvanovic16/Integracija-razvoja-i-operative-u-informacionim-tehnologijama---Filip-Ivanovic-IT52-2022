package com.chronoshop.payment.mapper;

import com.chronoshop.payment.domain.Payment;
import com.chronoshop.dto.PaymentDtos.PaymentResponse;

/**
 * Mapiranje Payment entiteta u DTO odgovor. Za razliku od monolita, orderNumber i
 * customerName ne dolaze sa JPA relacije ka Order (druga baza) vec su snimljeni na
 * samom Payment-u u trenutku kreiranja placanja.
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderNumber(),
                p.getStripePaymentIntentId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getCustomerEmail(),
                p.getCustomerName(),
                p.getPaidAt(),
                p.getCreatedAt()
        );
    }
}
