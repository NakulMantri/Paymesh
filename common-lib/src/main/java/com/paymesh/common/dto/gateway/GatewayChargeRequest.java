package com.paymesh.common.dto.gateway;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GatewayChargeRequest {
    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "Biller code is required")
    private String billerCode;

    @NotBlank(message = "Customer account number is required")
    private String customerAccountNumber;

    private Boolean simulateFailure;
    private Integer simulateDelayMs;

    public GatewayChargeRequest() {}

    public GatewayChargeRequest(String paymentReference, BigDecimal amount, String currency, String billerCode, String customerAccountNumber, Boolean simulateFailure, Integer simulateDelayMs) {
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.currency = currency;
        this.billerCode = billerCode;
        this.customerAccountNumber = customerAccountNumber;
        this.simulateFailure = simulateFailure;
        this.simulateDelayMs = simulateDelayMs;
    }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public Boolean getSimulateFailure() { return simulateFailure; }
    public void setSimulateFailure(Boolean simulateFailure) { this.simulateFailure = simulateFailure; }

    public Integer getSimulateDelayMs() { return simulateDelayMs; }
    public void setSimulateDelayMs(Integer simulateDelayMs) { this.simulateDelayMs = simulateDelayMs; }
}
