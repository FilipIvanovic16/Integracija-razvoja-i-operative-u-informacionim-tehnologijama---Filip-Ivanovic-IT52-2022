package com.chronoshop.notification.config;

import com.chronoshop.event.EventRouting;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * notification-service se pretplacuje na sva tri routing kljuca sa "chronoshop.events"
 * (order.created, payment.completed, payment.failed) - samo cita, nista ne publikuje.
 * Isti DLX/DLQ obrazac kao order-service i payment-service (feat/rabbitmq-events).
 */
@Configuration
public class RabbitMqConfig {

    private static final String QUEUE_PREFIX = "notification-service.";

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
        return queue(EventRouting.ORDER_CREATED);
    }

    @Bean
    public Queue orderCreatedDeadLetterQueue() {
        return dlq(EventRouting.ORDER_CREATED);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return queue(EventRouting.PAYMENT_COMPLETED);
    }

    @Bean
    public Queue paymentCompletedDeadLetterQueue() {
        return dlq(EventRouting.PAYMENT_COMPLETED);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return queue(EventRouting.PAYMENT_FAILED);
    }

    @Bean
    public Queue paymentFailedDeadLetterQueue() {
        return dlq(EventRouting.PAYMENT_FAILED);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange chronoShopEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(chronoShopEventsExchange).with(EventRouting.ORDER_CREATED);
    }

    @Bean
    public Binding orderCreatedDlqBinding(Queue orderCreatedDeadLetterQueue,
                                          TopicExchange chronoShopEventsDeadLetterExchange) {
        return BindingBuilder.bind(orderCreatedDeadLetterQueue).to(chronoShopEventsDeadLetterExchange)
                .with(EventRouting.ORDER_CREATED);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange chronoShopEventsExchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(chronoShopEventsExchange)
                .with(EventRouting.PAYMENT_COMPLETED);
    }

    @Bean
    public Binding paymentCompletedDlqBinding(Queue paymentCompletedDeadLetterQueue,
                                              TopicExchange chronoShopEventsDeadLetterExchange) {
        return BindingBuilder.bind(paymentCompletedDeadLetterQueue).to(chronoShopEventsDeadLetterExchange)
                .with(EventRouting.PAYMENT_COMPLETED);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange chronoShopEventsExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(chronoShopEventsExchange).with(EventRouting.PAYMENT_FAILED);
    }

    @Bean
    public Binding paymentFailedDlqBinding(Queue paymentFailedDeadLetterQueue,
                                           TopicExchange chronoShopEventsDeadLetterExchange) {
        return BindingBuilder.bind(paymentFailedDeadLetterQueue).to(chronoShopEventsDeadLetterExchange)
                .with(EventRouting.PAYMENT_FAILED);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    private Queue queue(String routingKey) {
        return QueueBuilder.durable(QUEUE_PREFIX + routingKey)
                .withArgument("x-dead-letter-exchange", EventRouting.DEAD_LETTER_EXCHANGE)
                .build();
    }

    private Queue dlq(String routingKey) {
        return QueueBuilder.durable(QUEUE_PREFIX + routingKey + ".dlq").build();
    }
}
