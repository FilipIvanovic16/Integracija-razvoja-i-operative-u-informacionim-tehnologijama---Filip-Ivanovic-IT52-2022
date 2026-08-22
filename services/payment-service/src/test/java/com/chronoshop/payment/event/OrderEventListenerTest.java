package com.chronoshop.payment.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.chronoshop.domain.enums.PaymentStatus;
import com.chronoshop.event.OrderCreatedEvent;
import com.chronoshop.payment.domain.Payment;
import com.chronoshop.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

  @Mock private PaymentRepository paymentRepository;

  @Test
  void onOrderCreated_createsPaymentStub_whenNoneExists() {
    when(paymentRepository.findByOrderId(42L)).thenReturn(Optional.empty());
    OrderCreatedEvent event =
        OrderCreatedEvent.of(
            42L, "ORD-1", "kupac@chronoshop.rs", "Petar Petrović", new BigDecimal("100.00"), "EUR");

    new OrderEventListener(paymentRepository).onOrderCreated(event);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(captor.capture());
    Payment saved = captor.getValue();
    assertThat(saved.getOrderId()).isEqualTo(42L);
    assertThat(saved.getOrderNumber()).isEqualTo("ORD-1");
    assertThat(saved.getStatus()).isEqualTo(PaymentStatus.REQUIRES_PAYMENT);
    assertThat(saved.getCustomerEmail()).isEqualTo("kupac@chronoshop.rs");
  }

  @Test
  void onOrderCreated_isIdempotent_whenStubAlreadyExists() {
    when(paymentRepository.findByOrderId(42L)).thenReturn(Optional.of(new Payment()));
    OrderCreatedEvent event =
        OrderCreatedEvent.of(
            42L, "ORD-1", "kupac@chronoshop.rs", "Petar Petrović", new BigDecimal("100.00"), "EUR");

    new OrderEventListener(paymentRepository).onOrderCreated(event);

    verify(paymentRepository, never()).save(any());
  }
}
