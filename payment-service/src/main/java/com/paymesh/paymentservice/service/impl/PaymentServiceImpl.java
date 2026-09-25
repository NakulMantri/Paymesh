package com.paymesh.paymentservice.service.impl;

import com.paymesh.common.dto.payment.PaymentRequest;
import com.paymesh.common.dto.payment.PaymentResponse;
import com.paymesh.common.exception.ResourceNotFoundException;
import com.paymesh.paymentservice.entity.Payment;
import com.paymesh.paymentservice.repository.PaymentRepository;
import com.paymesh.paymentservice.saga.PaymentSagaOrchestrator;
import com.paymesh.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentSagaOrchestrator sagaOrchestrator;
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentSagaOrchestrator sagaOrchestrator, PaymentRepository paymentRepository) {
        this.sagaOrchestrator = sagaOrchestrator;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        return sagaOrchestrator.executePaymentSaga(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", paymentId));
        return sagaOrchestrator.mapToResponse(payment, "Payment found");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByIdempotencyKey(String idempotencyKey) {
        Payment payment = paymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "idempotencyKey", idempotencyKey));
        return sagaOrchestrator.mapToResponse(payment, "Payment found");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(p -> sagaOrchestrator.mapToResponse(p, "Payment found"))
                .collect(Collectors.toList());
    }
}
