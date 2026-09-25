package com.paymesh.common.exception;

import org.springframework.http.HttpStatus;

public class CircuitBreakerOpenException extends PayMeshException {
    public CircuitBreakerOpenException(String serviceName) {
        super(String.format("Service '%s' is currently unavailable. Circuit breaker is OPEN.", serviceName),
                HttpStatus.SERVICE_UNAVAILABLE, "CIRCUIT_BREAKER_OPEN");
    }
}
