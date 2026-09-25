package com.paymesh.common.dto.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GatewayRefundResponse {
    private String refundTransactionId;
    private String originalGatewayTransactionId;
    private GatewayStatus status;
    private BigDecimal amount;
    private String responseCode;
    private String responseMessage;
    private LocalDateTime timestamp;

    public GatewayRefundResponse() {}

    public GatewayRefundResponse(String refundTransactionId, String originalGatewayTransactionId, GatewayStatus status, BigDecimal amount, String responseCode, String responseMessage, LocalDateTime timestamp) {
        this.refundTransactionId = refundTransactionId;
        this.originalGatewayTransactionId = originalGatewayTransactionId;
        this.status = status;
        this.amount = amount;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }

    public String getOriginalGatewayTransactionId() { return originalGatewayTransactionId; }
    public void setOriginalGatewayTransactionId(String originalGatewayTransactionId) { this.originalGatewayTransactionId = originalGatewayTransactionId; }

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
