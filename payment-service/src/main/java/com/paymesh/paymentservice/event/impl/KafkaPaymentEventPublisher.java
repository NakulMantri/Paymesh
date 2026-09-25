package com.paymesh.paymentservice.event.impl;

import com.paymesh.common.events.PaymentEvent;
import com.paymesh.paymentservice.event.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaPaymentEventPublisher.class);
    public static final String PAYMENT_TOPIC = "paymesh-payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final boolean kafkaEnabled;

    public KafkaPaymentEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${spring.kafka.enabled:true}") boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Override
    public void publishPaymentEvent(PaymentEvent event) {
        log.info("Emitting PaymentEvent to Kafka topic [{}]: id={}, type={}, status={}, paymentId={}",
                PAYMENT_TOPIC, event.getEventId(), event.getEventType(), event.getStatus(), event.getPaymentId());

        if (kafkaTemplate != null && kafkaEnabled) {
            try {
                kafkaTemplate.send(PAYMENT_TOPIC, event.getPaymentId(), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send PaymentEvent to Kafka: {}", ex.getMessage());
                            } else {
                                log.info("Successfully delivered PaymentEvent offset: {}", 
                                        result.getRecordMetadata().offset());
                            }
                        });
            } catch (Exception e) {
                log.warn("Kafka event dispatch failed gracefully: {}", e.getMessage());
            }
        }
    }
}
