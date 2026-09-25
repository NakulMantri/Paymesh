package com.paymesh.common.dto.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GatewayChargeResponse {
    private String gatewayTransactionId;
    private String paymentReference;
    private GatewayStatus status;
    private BigDecimal amount;
    private String responseCode;
    private String responseMessage;
    private LocalDateTime timestamp;

    public GatewayChargeResponse() {}

    public GatewayChargeResponse(String gatewayTransactionId, String paymentReference, GatewayStatus status, BigDecimal amount, String responseCode, String responseMessage, LocalDateTime timestamp) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.paymentReference = paymentReference;
        this.status = status;
        this.amount = amount;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public GatewayStatus getStatus() { return status; }
    public void setStatus(GatewayStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
