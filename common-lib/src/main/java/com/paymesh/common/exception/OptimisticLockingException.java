package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockingException extends PayMeshException {
    public OptimisticLockingException(String message) {
        super(message, HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION_CONFLICT");
    }
}
