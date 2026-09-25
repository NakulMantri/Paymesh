package com.paymesh.common.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Biller code is required")
    private String billerCode;

    @NotBlank(message = "Customer account number is required")
    private String customerAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency = "USD";
    private PaymentMethod paymentMethod = PaymentMethod.WALLET;
    private String idempotencyKey;

    private Boolean simulateGatewayFailure;
    private Integer simulateGatewayDelayMs;

    public PaymentRequest() {}

    public PaymentRequest(Long userId, String billerCode, String customerAccountNumber, BigDecimal amount, String currency, PaymentMethod paymentMethod, String idempotencyKey, Boolean simulateGatewayFailure, Integer simulateGatewayDelayMs) {
        this.userId = userId;
        this.billerCode = billerCode;
        this.customerAccountNumber = customerAccountNumber;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.WALLET;
        this.idempotencyKey = idempotencyKey;
        this.simulateGatewayFailure = simulateGatewayFailure;
        this.simulateGatewayDelayMs = simulateGatewayDelayMs;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Boolean getSimulateGatewayFailure() { return simulateGatewayFailure; }
    public void setSimulateGatewayFailure(Boolean simulateGatewayFailure) { this.simulateGatewayFailure = simulateGatewayFailure; }

    public Integer getSimulateGatewayDelayMs() { return simulateGatewayDelayMs; }
    public void setSimulateGatewayDelayMs(Integer simulateGatewayDelayMs) { this.simulateGatewayDelayMs = simulateGatewayDelayMs; }
}
