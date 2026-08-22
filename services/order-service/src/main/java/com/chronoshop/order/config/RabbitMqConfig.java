package com.chronoshop.order.config;

import com.chronoshop.event.EventRouting;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * order-service publikuje order.created i konzumira payment.completed/payment.failed sa iste topic
 * razmene "chronoshop.events". Svaki red za slusanje ima dead-letter razmenu - posle iscrpljenih
 * retry pokusaja (vidi application.properties, spring.rabbitmq.listener.simple.retry.*), Spring
 * AMQP odbija poruku bez requeue-a i ona zavrsava u odgovarajucem DLQ-u umesto da se gubi ili
 * blokira red.
 */
@Configuration
public class RabbitMqConfig {

  @Bean
  public TopicExchange chronoShopEventsExchange() {
    return ExchangeBuilder.topicExchange(EventRouting.EXCHANGE).durable(true).build();
  }

  @Bean
  public TopicExchange chronoShopEventsDeadLetterExchange() {
    return ExchangeBuilder.topicExchange(EventRouting.DEAD_LETTER_EXCHANGE).durable(true).build();
  }

  @Bean
  public Queue paymentCompletedQueue() {
    return QueueBuilder.durable("order-service." + EventRouting.PAYMENT_COMPLETED)
        .withArgument("x-dead-letter-exchange", EventRouting.DEAD_LETTER_EXCHANGE)
        .build();
  }

  @Bean
  public Queue paymentCompletedDeadLetterQueue() {
    return QueueBuilder.durable("order-service." + EventRouting.PAYMENT_COMPLETED + ".dlq").build();
  }

  @Bean
  public Queue paymentFailedQueue() {
    return QueueBuilder.durable("order-service." + EventRouting.PAYMENT_FAILED)
        .withArgument("x-dead-letter-exchange", EventRouting.DEAD_LETTER_EXCHANGE)
        .build();
  }

  @Bean
  public Queue paymentFailedDeadLetterQueue() {
    return QueueBuilder.durable("order-service." + EventRouting.PAYMENT_FAILED + ".dlq").build();
  }

  @Bean
  public Binding paymentCompletedBinding(
      Queue paymentCompletedQueue, TopicExchange chronoShopEventsExchange) {
    return BindingBuilder.bind(paymentCompletedQueue)
        .to(chronoShopEventsExchange)
        .with(EventRouting.PAYMENT_COMPLETED);
  }

  @Bean
  public Binding paymentCompletedDlqBinding(
      Queue paymentCompletedDeadLetterQueue, TopicExchange chronoShopEventsDeadLetterExchange) {
    return BindingBuilder.bind(paymentCompletedDeadLetterQueue)
        .to(chronoShopEventsDeadLetterExchange)
        .with(EventRouting.PAYMENT_COMPLETED);
  }

  @Bean
  public Binding paymentFailedBinding(
      Queue paymentFailedQueue, TopicExchange chronoShopEventsExchange) {
    return BindingBuilder.bind(paymentFailedQueue)
        .to(chronoShopEventsExchange)
        .with(EventRouting.PAYMENT_FAILED);
  }

  @Bean
  public Binding paymentFailedDlqBinding(
      Queue paymentFailedDeadLetterQueue, TopicExchange chronoShopEventsDeadLetterExchange) {
    return BindingBuilder.bind(paymentFailedDeadLetterQueue)
        .to(chronoShopEventsDeadLetterExchange)
        .with(EventRouting.PAYMENT_FAILED);
  }

  @Bean
  public MessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }
}
