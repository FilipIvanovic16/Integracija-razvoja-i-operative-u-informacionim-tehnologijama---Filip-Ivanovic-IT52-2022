package com.chronoshop.event;

/**
 * Nazivi RabbitMQ topic razmene i routing kljuceva za asinhronu komunikaciju porudzbina ->
 * placanje. Deljeno izmedju order-service i payment-service (i kasnije notification-service) da se
 * izbegnu magicni stringovi po servisima.
 */
public final class EventRouting {

  private EventRouting() {}

  public static final String EXCHANGE = "chronoshop.events";
  public static final String DEAD_LETTER_EXCHANGE = "chronoshop.events.dlx";

  public static final String ORDER_CREATED = "order.created";
  public static final String PAYMENT_COMPLETED = "payment.completed";
  public static final String PAYMENT_FAILED = "payment.failed";
}
