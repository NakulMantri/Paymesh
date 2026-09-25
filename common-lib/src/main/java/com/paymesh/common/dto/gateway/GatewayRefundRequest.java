package com.paymesh.common.dto.gateway;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GatewayRefundRequest {
    @NotBlank(message = "Original gateway transaction ID is required")
    private String gatewayTransactionId;

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String reason;

    public GatewayRefundRequest() {}

    public GatewayRefundRequest(String gatewayTransactionId, String paymentReference, BigDecimal amount, String reason) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.reason = reason;
    }

    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
