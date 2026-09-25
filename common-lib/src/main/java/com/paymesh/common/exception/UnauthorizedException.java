package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends PayMeshException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
