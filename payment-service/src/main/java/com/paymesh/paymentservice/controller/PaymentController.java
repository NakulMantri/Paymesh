package com.paymesh.paymentservice.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.payment.PaymentRequest;
import com.paymesh.common.dto.payment.PaymentResponse;
import com.paymesh.common.util.SecurityConstants;
import com.paymesh.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) Long authenticatedUserId) {

        // Auto-populate userId from authenticated JWT if not in body
        if (request.getUserId() == null && authenticatedUserId != null) {
            request.setUserId(authenticatedUserId);
        }

        // Auto-populate Idempotency key from header if not in body
        if ((request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment processed successfully", response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/idempotency/{key}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByIdempotencyKey(@PathVariable String key) {
        PaymentResponse response = paymentService.getPaymentByIdempotencyKey(key);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentResponse> response = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Missing X-User-Id header", "BAD_REQUEST"));
        }
        List<PaymentResponse> response = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
