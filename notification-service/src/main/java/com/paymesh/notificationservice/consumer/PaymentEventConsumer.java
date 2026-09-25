package com.paymesh.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymesh.common.events.PaymentEvent;
import com.paymesh.notificationservice.entity.NotificationLog;
import com.paymesh.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentEventConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "paymesh-payment-events", groupId = "paymesh-notification-group")
    public void consumePaymentEvent(String eventPayload) {
        try {
            log.info("Kafka Consumer received payload on topic [paymesh-payment-events]: {}", eventPayload);
            PaymentEvent event = objectMapper.readValue(eventPayload, PaymentEvent.class);

            handlePaymentEvent(event);
        } catch (Exception e) {
            log.error("Failed to parse and process Kafka PaymentEvent: {}", e.getMessage(), e);
        }
    }

    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Processing PaymentEvent: Type={}, Status={}, PaymentId={}, Amount=${}, Biller={}",
                event.getEventType(), event.getStatus(), event.getPaymentId(), event.getAmount(), event.getBillerCode());

        String emailSubject;
        String emailBody;
        String smsBody;

        if ("PAYMENT_COMPLETED".equalsIgnoreCase(event.getEventType())) {
            emailSubject = "PayMesh Confirmation: Payment of $" + event.getAmount() + " to " + event.getBillerCode() + " Successful";
            emailBody = String.format(
                    "Hello,\n\nYour bill payment of $%s to %s (Account: %s) was processed successfully.\n" +
                    "Payment ID: %s\nStatus: %s\nTimestamp: %s\n\nThank you for using PayMesh!",
                    event.getAmount(), event.getBillerCode(), event.getCustomerAccountNumber(),
                    event.getPaymentId(), event.getStatus(), event.getTimestamp());
            smsBody = String.format("PayMesh: Paid $%s to %s for acct %s. Ref: %s. Status: SUCCESS.",
                    event.getAmount(), event.getBillerCode(), event.getCustomerAccountNumber(), event.getPaymentId());
        } else {
            emailSubject = "PayMesh Alert: Payment of $" + event.getAmount() + " to " + event.getBillerCode() + " Failed";
            emailBody = String.format(
                    "Hello,\n\nWe encountered an issue processing your bill payment of $%s to %s.\n" +
                    "Reason: %s\nAny debited funds have been automatically refunded to your wallet via Saga rollback.\n" +
                    "Payment ID: %s\nStatus: %s\n\nIf you have questions, please contact support.",
                    event.getAmount(), event.getBillerCode(), event.getFailureReason(),
                    event.getPaymentId(), event.getStatus());
            smsBody = String.format("PayMesh Alert: Payment of $%s to %s failed (%s). Wallet refunded. Ref: %s.",
                    event.getAmount(), event.getBillerCode(), event.getFailureReason(), event.getPaymentId());
        }

        // Persist simulated EMAIL dispatch log
        NotificationLog emailLog = new NotificationLog(
                event.getEventId() != null ? event.getEventId() : "EVT-" + event.getPaymentId(),
                event.getUserId(),
                event.getPaymentId(),
                "EMAIL",
                "user" + event.getUserId() + "@paymesh.io",
                emailSubject,
                emailBody,
                "DELIVERED",
                event.getTraceId()
        );
        notificationRepository.save(emailLog);

        // Persist simulated SMS dispatch log
        NotificationLog smsLog = new NotificationLog(
                event.getEventId() != null ? event.getEventId() : "EVT-" + event.getPaymentId(),
                event.getUserId(),
                event.getPaymentId(),
                "SMS",
                "+1-555-0199",
                "SMS Alert",
                smsBody,
                "DELIVERED",
                event.getTraceId()
        );
        notificationRepository.save(smsLog);

        log.info("Successfully dispatched EMAIL & SMS notifications for payment {}", event.getPaymentId());
    }
}
