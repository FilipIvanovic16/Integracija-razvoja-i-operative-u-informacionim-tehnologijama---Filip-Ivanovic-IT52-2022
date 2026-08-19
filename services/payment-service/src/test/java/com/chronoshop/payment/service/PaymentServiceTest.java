package com.chronoshop.payment.service;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.payment.client.OrderClient;
import com.chronoshop.payment.domain.Payment;
import com.chronoshop.payment.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stripe SDK poziva se preko static metoda (PaymentIntent.create, Webhook.constructEvent),
 * pa se u testovima mock-uju preko Mockito.mockStatic - "unit testovi sa mock-ovanim
 * Stripe klijentom" iz plana.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private final OrderClient orderClient = mock(OrderClient.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);

    private final PaymentService paymentService = new PaymentService(
            orderClient, paymentRepository, "whsec_test", "sk_test_dummy");

    private static final OrderResponse PENDING_ORDER = new OrderResponse(
            42L, "ORD-20260818-ABCDEF", OrderStatus.PENDING, new BigDecimal("12000.00"),
            "kupac@chronoshop.rs", "Petar Petrović", "Ulica 1", "Novi Sad", "21000", "Srbija",
            LocalDateTime.now(), List.of());

    @Test
    void createPaymentIntent_returnsCheckoutSession_whenOrderPending() {
        when(orderClient.getOrder(42L, 1L, false)).thenReturn(PENDING_ORDER);
        when(paymentRepository.findByOrderId(42L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntent fakeIntent = mock(PaymentIntent.class);
        when(fakeIntent.getId()).thenReturn("pi_test_123");
        when(fakeIntent.getClientSecret()).thenReturn("pi_test_123_secret_abc");

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(fakeIntent);

            var response = paymentService.createPaymentIntent(1L, false, 42L);

            assertThat(response.clientSecret()).isEqualTo("pi_test_123_secret_abc");
            assertThat(response.paymentIntentId()).isEqualTo("pi_test_123");
            assertThat(response.orderNumber()).isEqualTo("ORD-20260818-ABCDEF");
            assertThat(response.amount()).isEqualByComparingTo("12000.00");
        }

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPaymentIntent_throwsBadRequest_whenOrderNotPending() {
        OrderResponse paidOrder = new OrderResponse(42L, "ORD-1", OrderStatus.PAID, new BigDecimal("100.00"),
                "kupac@chronoshop.rs", "Petar Petrović", null, null, null, null, LocalDateTime.now(), List.of());
        when(orderClient.getOrder(42L, 1L, false)).thenReturn(paidOrder);

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            assertThatThrownBy(() -> paymentService.createPaymentIntent(1L, false, 42L))
                    .isInstanceOf(BadRequestException.class);

            mocked.verifyNoInteractions();
        }
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleWebhook_throwsBadRequest_whenSignatureInvalid() {
        try (MockedStatic<Webhook> mocked = Mockito.mockStatic(Webhook.class)) {
            mocked.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(new SignatureVerificationException("bad signature", "sig_header"));

            assertThatThrownBy(() -> paymentService.handleWebhook("{}", "bad-sig"))
                    .isInstanceOf(BadRequestException.class);
        }
        verifyNoInteractions(paymentRepository);
    }
}
