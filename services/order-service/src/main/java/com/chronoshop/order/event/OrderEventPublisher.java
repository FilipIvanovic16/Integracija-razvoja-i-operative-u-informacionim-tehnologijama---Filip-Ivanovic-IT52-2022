package com.chronoshop.order.event;

import com.chronoshop.event.EventRouting;
import com.chronoshop.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;

  public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishOrderCreated(OrderCreatedEvent event) {
    rabbitTemplate.convertAndSend(EventRouting.EXCHANGE, EventRouting.ORDER_CREATED, event);
    log.info("Objavljen order.created za porudžbinu {}.", event.orderNumber());
  }
}
