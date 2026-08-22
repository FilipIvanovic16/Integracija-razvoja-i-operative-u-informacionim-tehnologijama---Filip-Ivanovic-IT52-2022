package com.chronoshop.order.event;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.event.PaymentCompletedEvent;
import com.chronoshop.event.PaymentFailedEvent;
import com.chronoshop.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Zamenjuje REST poziv koji je payment-service ranije radio (feat/payment-service PR) da bi javio
 * order-service-u novi status - sada to ide asinhrono preko RabbitMQ, kako specifikacija zahteva.
 */
@Component
public class PaymentEventListener {

  private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

  private final OrderService orderService;

  public PaymentEventListener(OrderService orderService) {
    this.orderService = orderService;
  }

  @RabbitListener(queues = "order-service.payment.completed")
  public void onPaymentCompleted(PaymentCompletedEvent event) {
    log.info("Primljen payment.completed za porudžbinu {} - status -> PAID.", event.orderNumber());
    orderService.updateStatus(event.orderId(), OrderStatus.PAID);
  }

  @RabbitListener(queues = "order-service.payment.failed")
  public void onPaymentFailed(PaymentFailedEvent event) {
    log.info(
        "Primljen payment.failed za porudžbinu {} - status -> CANCELLED.", event.orderNumber());
    orderService.updateStatus(event.orderId(), OrderStatus.CANCELLED);
  }
}
