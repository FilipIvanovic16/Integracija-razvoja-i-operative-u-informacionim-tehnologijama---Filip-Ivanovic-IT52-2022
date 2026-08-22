package com.chronoshop.payment.domain;

import com.chronoshop.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "payments",
    uniqueConstraints = {@UniqueConstraint(columnNames = "stripe_payment_intent_id")})
@Getter
@Setter
@NoArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Order zivi u orderdb (order-service) - samo orderId + snimljeni orderNumber/
  // customerName/customerEmail u trenutku kreiranja placanja (isto nacelo kao kod
  // order-service prema catalog/auth).
  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "order_number", nullable = false, length = 40)
  private String orderNumber;

  @Column(name = "stripe_payment_intent_id", length = 100)
  private String stripePaymentIntentId;

  /** ID poslednjeg obrađenog Stripe događaja. */
  @Column(name = "stripe_event_id", length = 100)
  private String stripeEventId;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "EUR";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentStatus status = PaymentStatus.REQUIRES_PAYMENT;

  @Column(length = 160)
  private String customerEmail;

  @Column(length = 160)
  private String customerName;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime paidAt;
}
