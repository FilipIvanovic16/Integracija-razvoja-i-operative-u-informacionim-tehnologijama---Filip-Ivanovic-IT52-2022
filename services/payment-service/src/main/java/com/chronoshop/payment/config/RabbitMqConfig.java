package com.chronoshop.payment.config;

import com.chronoshop.event.EventRouting;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * payment-service konzumira order.created (eagerly kreira Payment "stub" da admin pregled
 * transakcija vidi porudzbinu i pre nego sto kupac krene na plaćanje) i publikuje
 * payment.completed/payment.failed na istu topic razmenu.
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
  public Queue orderCreatedQueue() {
    return QueueBuilder.durable("payment-service." + EventRouting.ORDER_CREATED)
        .withArgument("x-dead-letter-exchange", EventRouting.DEAD_LETTER_EXCHANGE)
        .build();
  }

  @Bean
  public Queue orderCreatedDeadLetterQueue() {
    return QueueBuilder.durable("payment-service." + EventRouting.ORDER_CREATED + ".dlq").build();
  }

  @Bean
  public Binding orderCreatedBinding(
      Queue orderCreatedQueue, TopicExchange chronoShopEventsExchange) {
    return BindingBuilder.bind(orderCreatedQueue)
        .to(chronoShopEventsExchange)
        .with(EventRouting.ORDER_CREATED);
  }

  @Bean
  public Binding orderCreatedDlqBinding(
      Queue orderCreatedDeadLetterQueue, TopicExchange chronoShopEventsDeadLetterExchange) {
    return BindingBuilder.bind(orderCreatedDeadLetterQueue)
        .to(chronoShopEventsDeadLetterExchange)
        .with(EventRouting.ORDER_CREATED);
  }

  @Bean
  public MessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }
}
