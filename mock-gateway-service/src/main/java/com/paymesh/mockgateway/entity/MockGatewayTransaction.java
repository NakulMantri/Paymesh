package com.paymesh.mockgateway.entity;

import com.paymesh.common.dto.gateway.GatewayStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_gateway_transactions", indexes = {
        @Index(name = "idx_gw_tx_id", columnList = "gatewayTransactionId"),
        @Index(name = "idx_gw_pay_ref", columnList = "paymentReference")
})
public class MockGatewayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String gatewayTransactionId;

    @Column(nullable = false, length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatewayStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(length = 50)
    private String billerCode;

    @Column(length = 50)
    private String customerAccountNumber;

    @Column(length = 20)
    private String responseCode;

    @Column(length = 255)
    private String responseMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MockGatewayTransaction() {}

    public MockGatewayTransaction(String gatewayTransactionId, String paymentReference, GatewayStatus status, BigDecimal amount, String currency, String billerCode, String customerAccountNumber, String responseCode, String responseMessage) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.paymentReference = paymentReference;
        this.status = status;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.billerCode = billerCode;
        this.customerAccountNumber = customerAccountNumber;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public GatewayStatus getStatus() { return status; }
    public void setStatus(GatewayStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
