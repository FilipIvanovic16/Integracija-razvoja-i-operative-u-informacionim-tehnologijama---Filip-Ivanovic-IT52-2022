package com.chronoshop.domain;

import com.chronoshop.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(columnNames = "stripe_payment_intent_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    /** ID poslednjeg obrađenog Stripe događaja  */
    @Column(name = "stripe_event_id", length = 100)
    private String stripeEventId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.REQUIRES_PAYMENT;

    /** Email kupca zabeležen na Stripe transakciji. */
    @Column(length = 160)
    private String customerEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;
}
