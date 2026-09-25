package com.paymesh.paymentservice.service;

import com.paymesh.common.dto.payment.PaymentRequest;
import com.paymesh.common.dto.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse getPaymentById(String paymentId);
    PaymentResponse getPaymentByIdempotencyKey(String idempotencyKey);
    List<PaymentResponse> getPaymentsByUserId(Long userId);
}
