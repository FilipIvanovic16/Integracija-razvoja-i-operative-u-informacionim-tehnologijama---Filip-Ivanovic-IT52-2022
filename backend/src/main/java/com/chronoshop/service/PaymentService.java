package com.chronoshop.service;

import com.chronoshop.domain.Order;
import com.chronoshop.domain.Payment;
import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.domain.enums.PaymentStatus;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.PaymentDtos.CheckoutSessionResponse;
import com.chronoshop.dto.PaymentDtos.PaymentResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.OrderRepository;
import com.chronoshop.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Integracija sa Stripe payment procesorom (test mode).
 * - kreira PaymentIntent i vraća client secret frontendu;
 * - obrađuje webhook događaje uz verifikaciju potpisa i idempotentnost.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String CURRENCY = "eur";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final String webhookSecret;
    private final String publishableHint;

    public PaymentService(OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          @Value("${stripe.webhook.secret}") String webhookSecret,
                          @Value("${stripe.api.secret-key}") String secretKey) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.webhookSecret = webhookSecret;
        // Samo informativni nagoveštaj da je u test režimu; pravi publishable ključ stoji na frontendu
        this.publishableHint = secretKey != null && secretKey.startsWith("sk_test") ? "test_mode" : "live_mode";
    }

    /**
     * Kreira (ili reuse-uje) Stripe PaymentIntent za datu porudžbinu i vraća client secret.
     */
    @Transactional
    public CheckoutSessionResponse createPaymentIntent(Long userId, boolean isAdmin, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Porudžbina", orderId));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Porudžbina", orderId);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Plaćanje je moguće samo za porudžbine na čekanju.");
        }

        long amountInCents = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(CURRENCY)
                    .setReceiptEmail(order.getUser().getEmail())
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .putMetadata("orderNumber", order.getOrderNumber())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = paymentRepository.findByOrderId(order.getId()).orElseGet(Payment::new);
            payment.setOrder(order);
            payment.setStripePaymentIntentId(intent.getId());
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency(CURRENCY.toUpperCase());
            payment.setStatus(PaymentStatus.REQUIRES_PAYMENT);
            payment.setCustomerEmail(order.getUser().getEmail());
            paymentRepository.save(payment);

            return new CheckoutSessionResponse(
                    intent.getClientSecret(),
                    intent.getId(),
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    CURRENCY.toUpperCase(),
                    publishableHint
            );
        } catch (StripeException e) {
            log.error("Greška pri kreiranju Stripe PaymentIntent-a: {}", e.getMessage());
            throw new BadRequestException("Nije moguće inicirati plaćanje. " + e.getMessage());
        }
    }

    /**
     * Obrađuje Stripe webhook poziv. Verifikuje kriptografski potpis (Stripe-Signature)
     * i obrađuje događaj idempotentno (isti event se primenjuje samo jednom).
     */
    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Odbijen webhook sa nevalidnim potpisom.");
            throw new BadRequestException("Nevalidan potpis webhook zahteva.");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentIntent(event, PaymentStatus.SUCCEEDED);
            case "payment_intent.payment_failed" -> handlePaymentIntent(event, PaymentStatus.FAILED);
            case "payment_intent.canceled" -> handlePaymentIntent(event, PaymentStatus.CANCELLED);
            default -> log.debug("Ignorisan Stripe događaj tipa: {}", event.getType());
        }
    }

    private void handlePaymentIntent(Event event, PaymentStatus newStatus) {
        Optional<StripeObject> deserialized = event.getDataObjectDeserializer().getObject();
        if (deserialized.isEmpty() || !(deserialized.get() instanceof PaymentIntent intent)) {
            log.warn("Webhook događaj {} nema iskoristiv PaymentIntent objekat.", event.getId());
            return;
        }

        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId()).orElse(null);
        if (payment == null) {
            log.warn("Nije pronađeno plaćanje za PaymentIntent {}.", intent.getId());
            return;
        }

        // Idempotentnost: ako je ovaj event već obrađen, ne radimo ništa
        if (event.getId().equals(payment.getStripeEventId())) {
            log.info("Webhook događaj {} je već obrađen — preskačem.", event.getId());
            return;
        }

        payment.setStatus(newStatus);
        payment.setStripeEventId(event.getId());
        if (intent.getReceiptEmail() != null) {
            payment.setCustomerEmail(intent.getReceiptEmail());
        }

        Order order = payment.getOrder();
        if (newStatus == PaymentStatus.SUCCEEDED) {
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.PAID);
        } else if (newStatus == PaymentStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
        log.info("Plaćanje za porudžbinu {} ažurirano na status {}.", order.getOrderNumber(), newStatus);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listTransactions(PaymentStatus status, Pageable pageable) {
        var page = (status == null)
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findByStatus(status, pageable);
        return PageResponse.from(page, EntityMapper::toPaymentResponse);
    }
}
