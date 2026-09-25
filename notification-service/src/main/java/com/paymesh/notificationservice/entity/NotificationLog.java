package com.paymesh.notificationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notif_user_id", columnList = "userId"),
        @Index(name = "idx_notif_payment_id", columnList = "paymentId")
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventId;

    private Long userId;

    @Column(length = 100)
    private String paymentId;

    @Column(nullable = false, length = 30)
    private String channel; // EMAIL, SMS, PUSH

    @Column(length = 150)
    private String recipient;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String messageBody;

    @Column(nullable = false, length = 30)
    private String status = "SENT";

    @Column(length = 100)
    private String traceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public NotificationLog() {}

    public NotificationLog(String eventId, Long userId, String paymentId, String channel, String recipient, String subject, String messageBody, String status, String traceId) {
        this.eventId = eventId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.messageBody = messageBody;
        this.status = status != null ? status : "SENT";
        this.traceId = traceId;
    }

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
