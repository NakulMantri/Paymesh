package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends PayMeshException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE");
    }
}
