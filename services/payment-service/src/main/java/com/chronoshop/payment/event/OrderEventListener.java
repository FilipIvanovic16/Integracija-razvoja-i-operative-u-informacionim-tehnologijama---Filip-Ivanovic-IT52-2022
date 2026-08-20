package com.chronoshop.payment.event;

import com.chronoshop.domain.enums.PaymentStatus;
import com.chronoshop.event.OrderCreatedEvent;
import com.chronoshop.payment.domain.Payment;
import com.chronoshop.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Eagerly kreira Payment "stub" (status REQUIRES_PAYMENT) cim je porudzbina kreirana,
 * pre nego sto kupac uopste pokrene checkout. Admin pregled transakcija time vidi
 * porudzbinu odmah; PaymentService.createPaymentIntent kasnije nadje ovaj isti red
 * preko findByOrderId umesto da pravi novi.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final PaymentRepository paymentRepository;

    public OrderEventListener(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @RabbitListener(queues = "payment-service.order.created")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        if (paymentRepository.findByOrderId(event.orderId()).isPresent()) {
            log.debug("Payment stub za porudžbinu {} već postoji - preskačem.", event.orderNumber());
            return;
        }
        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setOrderNumber(event.orderNumber());
        payment.setAmount(event.totalAmount());
        payment.setCurrency(event.currency());
        payment.setStatus(PaymentStatus.REQUIRES_PAYMENT);
        payment.setCustomerEmail(event.customerEmail());
        payment.setCustomerName(event.customerName());
        paymentRepository.save(payment);
        log.info("Primljen order.created - kreiran payment stub za porudžbinu {}.", event.orderNumber());
    }
}
