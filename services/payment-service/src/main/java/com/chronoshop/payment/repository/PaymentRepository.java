package com.chronoshop.payment.repository;

import com.chronoshop.domain.enums.PaymentStatus;
import com.chronoshop.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

  Optional<Payment> findByOrderId(Long orderId);

  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
