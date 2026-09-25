package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRequestException extends PayMeshException {
    public DuplicateRequestException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_IDEMPOTENT_REQUEST");
    }
}
