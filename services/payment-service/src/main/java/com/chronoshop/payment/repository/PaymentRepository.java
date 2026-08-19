package com.chronoshop.payment.repository;

import com.chronoshop.payment.domain.Payment;
import com.chronoshop.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Payment> findByOrderId(Long orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
