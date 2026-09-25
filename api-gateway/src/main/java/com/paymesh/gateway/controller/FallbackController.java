package com.paymesh.gateway.controller;

import com.paymesh.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<ApiResponse<Void>>> userServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure("User Service is temporarily unavailable. Please try again shortly.", "SERVICE_UNAVAILABLE")));
    }

    @GetMapping("/wallet-service")
    public Mono<ResponseEntity<ApiResponse<Void>>> walletServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure("Wallet Service is temporarily unavailable. Please try again shortly.", "SERVICE_UNAVAILABLE")));
    }

    @GetMapping("/biller-service")
    public Mono<ResponseEntity<ApiResponse<Void>>> billerServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure("Biller Service is temporarily unavailable. Please try again shortly.", "SERVICE_UNAVAILABLE")));
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<ApiResponse<Void>>> paymentServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure("Payment Service is temporarily unavailable. Please try again shortly.", "SERVICE_UNAVAILABLE")));
    }
}
