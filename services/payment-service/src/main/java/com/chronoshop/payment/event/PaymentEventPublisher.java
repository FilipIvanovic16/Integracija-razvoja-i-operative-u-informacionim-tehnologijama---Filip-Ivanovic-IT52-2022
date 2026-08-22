package com.chronoshop.payment.event;

import com.chronoshop.event.EventRouting;
import com.chronoshop.event.PaymentCompletedEvent;
import com.chronoshop.event.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;

  public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishCompleted(PaymentCompletedEvent event) {
    rabbitTemplate.convertAndSend(EventRouting.EXCHANGE, EventRouting.PAYMENT_COMPLETED, event);
    log.info("Objavljen payment.completed za porudžbinu {}.", event.orderNumber());
  }

  public void publishFailed(PaymentFailedEvent event) {
    rabbitTemplate.convertAndSend(EventRouting.EXCHANGE, EventRouting.PAYMENT_FAILED, event);
    log.info("Objavljen payment.failed za porudžbinu {}.", event.orderNumber());
  }
}
