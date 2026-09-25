package com.paymesh.mockgateway.service.impl;

import com.paymesh.common.dto.gateway.*;
import com.paymesh.common.exception.PaymentProcessingException;
import com.paymesh.common.exception.ResourceNotFoundException;
import com.paymesh.mockgateway.entity.MockGatewayTransaction;
import com.paymesh.mockgateway.repository.MockGatewayRepository;
import com.paymesh.mockgateway.service.MockGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class MockGatewayServiceImpl implements MockGatewayService {

    private static final Logger log = LoggerFactory.getLogger(MockGatewayServiceImpl.class);
    private final Random random = new Random();

    private final MockGatewayRepository repository;
    private final AtomicInteger failureRatePercent = new AtomicInteger(0);
    private final AtomicInteger defaultDelayMs = new AtomicInteger(50);

    public MockGatewayServiceImpl(
            MockGatewayRepository repository,
            @Value("${gateway.simulation.default-failure-rate-percent:0}") int initialFailureRate,
            @Value("${gateway.simulation.default-delay-ms:50}") int initialDelayMs) {
        this.repository = repository;
        this.failureRatePercent.set(initialFailureRate);
        this.defaultDelayMs.set(initialDelayMs);
    }

    @Override
    public GatewayChargeResponse processCharge(GatewayChargeRequest request) {
        log.info("Processing gateway charge for ref: {}, amount: ${}, biller: {}", 
                request.getPaymentReference(), request.getAmount(), request.getBillerCode());

        // Idempotency: return existing transaction if already processed
        Optional<MockGatewayTransaction> existing = repository.findByPaymentReference(request.getPaymentReference());
        if (existing.isPresent()) {
            MockGatewayTransaction tx = existing.get();
            log.info("Returning existing gateway transaction {}", tx.getGatewayTransactionId());
            return new GatewayChargeResponse(
                    tx.getGatewayTransactionId(),
                    tx.getPaymentReference(),
                    tx.getStatus(),
                    tx.getAmount(),
                    tx.getResponseCode(),
                    tx.getResponseMessage(),
                    tx.getCreatedAt()
            );
        }

        // Handle simulated latency
        int delay = request.getSimulateDelayMs() != null ? request.getSimulateDelayMs() : defaultDelayMs.get();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Determine outcome: simulate failure via request flag or global failure rate
        boolean shouldFail = Boolean.TRUE.equals(request.getSimulateFailure()) ||
                (failureRatePercent.get() > 0 && random.nextInt(100) < failureRatePercent.get());

        String gatewayTxId = "GW-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        if (shouldFail) {
            log.warn("Mock Gateway simulating FAILURE for payment reference: {}", request.getPaymentReference());
            MockGatewayTransaction failedTx = new MockGatewayTransaction(
                    gatewayTxId,
                    request.getPaymentReference(),
                    GatewayStatus.FAILED,
                    request.getAmount(),
                    request.getCurrency(),
                    request.getBillerCode(),
                    request.getCustomerAccountNumber(),
                    "GW_502_DECLINED",
                    "Simulated processor network failure or card declined"
            );
            repository.save(failedTx);

            throw new PaymentProcessingException(
                    "3rd-party Payment Processor declined transaction for payment reference: " + request.getPaymentReference());
        }

        // Successful charge
        MockGatewayTransaction successTx = new MockGatewayTransaction(
                gatewayTxId,
                request.getPaymentReference(),
                GatewayStatus.SUCCESS,
                request.getAmount(),
                request.getCurrency(),
                request.getBillerCode(),
                request.getCustomerAccountNumber(),
                "GW_200_APPROVED",
                "Transaction authorized and captured successfully"
        );
        MockGatewayTransaction saved = repository.save(successTx);

        return new GatewayChargeResponse(
                saved.getGatewayTransactionId(),
                saved.getPaymentReference(),
                saved.getStatus(),
                saved.getAmount(),
                saved.getResponseCode(),
                saved.getResponseMessage(),
                saved.getCreatedAt()
        );
    }

    @Override
    public GatewayRefundResponse processRefund(GatewayRefundRequest request) {
        log.info("Processing gateway refund for original GW ID: {}, amount: ${}", 
                request.getGatewayTransactionId(), request.getAmount());

        MockGatewayTransaction originalTx = repository.findByGatewayTransactionId(request.getGatewayTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("GatewayTransaction", "gatewayTransactionId", request.getGatewayTransactionId()));

        String refundTxId = "GW-REFUND-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        return new GatewayRefundResponse(
                refundTxId,
                originalTx.getGatewayTransactionId(),
                GatewayStatus.SUCCESS,
                request.getAmount(),
                "GW_REFUND_200",
                "Refund settled successfully back to issuing method",
                LocalDateTime.now()
        );
    }

    @Override
    public Map<String, Object> getConfig() {
        return Map.of(
                "failureRatePercent", failureRatePercent.get(),
                "defaultDelayMs", defaultDelayMs.get()
        );
    }

    @Override
    public Map<String, Object> updateConfig(int failureRate, int delayMs) {
        failureRatePercent.set(Math.max(0, Math.min(100, failureRate)));
        defaultDelayMs.set(Math.max(0, delayMs));
        return getConfig();
    }
}
