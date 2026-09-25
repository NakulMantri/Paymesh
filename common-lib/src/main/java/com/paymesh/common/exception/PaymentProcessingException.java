package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends PayMeshException {
    public PaymentProcessingException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY_ERROR");
    }

    public PaymentProcessingException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
