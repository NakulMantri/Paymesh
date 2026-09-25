package com.paymesh.common.events;

import com.paymesh.common.dto.wallet.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletEvent {
    private String eventId;
    private Long walletId;
    private Long userId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String traceId;
    private LocalDateTime timestamp;

    public WalletEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public WalletEvent(String eventId, Long walletId, Long userId, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, String referenceId, String traceId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.walletId = walletId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceId = referenceId;
        this.traceId = traceId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
