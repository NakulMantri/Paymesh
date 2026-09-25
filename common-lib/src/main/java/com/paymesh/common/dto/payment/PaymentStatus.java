package com.paymesh.common.dto.payment;

public enum PaymentStatus {
    INITIATED,
    VALIDATING,
    WALLET_DEBITED,
    GATEWAY_PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED,
    COMPENSATED
}
