package com.paymesh.common.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WalletRefundRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Original debit reference is required")
    private String originalReferenceId;

    private String reason;

    public WalletRefundRequest() {}

    public WalletRefundRequest(Long userId, BigDecimal amount, String originalReferenceId, String reason) {
        this.userId = userId;
        this.amount = amount;
        this.originalReferenceId = originalReferenceId;
        this.reason = reason;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getOriginalReferenceId() { return originalReferenceId; }
    public void setOriginalReferenceId(String originalReferenceId) { this.originalReferenceId = originalReferenceId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
