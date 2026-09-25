package com.paymesh.common.events;

import com.paymesh.common.dto.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentEvent {
    private String eventId;
    private String eventType;
    private String paymentId;
    private Long userId;
    private String billerCode;
    private String customerAccountNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    private String traceId;
    private LocalDateTime timestamp;

    public PaymentEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PaymentEvent(String eventId, String eventType, String paymentId, Long userId, String billerCode, String customerAccountNumber, BigDecimal amount, PaymentStatus status, String failureReason, String traceId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.userId = userId;
        this.billerCode = billerCode;
        this.customerAccountNumber = customerAccountNumber;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.traceId = traceId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
