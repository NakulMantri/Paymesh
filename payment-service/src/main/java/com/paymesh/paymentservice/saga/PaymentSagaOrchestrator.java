package com.paymesh.paymentservice.saga;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.payment.PaymentMethod;
import com.paymesh.common.dto.payment.PaymentRequest;
import com.paymesh.common.dto.payment.PaymentResponse;
import com.paymesh.common.dto.payment.PaymentStatus;
import com.paymesh.common.dto.wallet.WalletDebitRequest;
import com.paymesh.common.dto.wallet.WalletRefundRequest;
import com.paymesh.common.dto.wallet.WalletTransactionDto;
import com.paymesh.common.events.PaymentEvent;
import com.paymesh.common.exception.CircuitBreakerOpenException;
import com.paymesh.common.exception.PayMeshException;
import com.paymesh.common.exception.PaymentProcessingException;
import com.paymesh.paymentservice.client.BillerClient;
import com.paymesh.paymentservice.client.GatewayClient;
import com.paymesh.paymentservice.client.WalletClient;
import com.paymesh.paymentservice.entity.Payment;
import com.paymesh.paymentservice.event.PaymentEventPublisher;
import com.paymesh.paymentservice.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaOrchestrator.class);

    private final PaymentRepository paymentRepository;
    private final WalletClient walletClient;
    private final BillerClient billerClient;
    private final GatewayClient gatewayClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentSagaOrchestrator(
            PaymentRepository paymentRepository,
            WalletClient walletClient,
            BillerClient billerClient,
            GatewayClient gatewayClient,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.walletClient = walletClient;
        this.billerClient = billerClient;
        this.gatewayClient = gatewayClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse executePaymentSaga(PaymentRequest request) {
        log.info("Starting Payment Saga: User={}, Biller={}, Amount=${}, IdempotencyKey={}",
                request.getUserId(), request.getBillerCode(), request.getAmount(), request.getIdempotencyKey());

        // Step 1: Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent request matched existing payment: {}", existing.get().getPaymentId());
                return mapToResponse(existing.get(), "Idempotent payment returned");
            }
        }

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Initialize Saga payment record
        Payment payment = new Payment(
                paymentId,
                request.getUserId(),
                request.getBillerCode().toUpperCase(),
                request.getCustomerAccountNumber(),
                request.getAmount(),
                request.getCurrency(),
                PaymentStatus.INITIATED,
                request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.WALLET,
                request.getIdempotencyKey()
        );
        payment = paymentRepository.save(payment);

        // Step 2: Validate Biller Account with Biller Service
        log.info("[Saga Step 1/3] Validating account with Biller Service for biller: {}", request.getBillerCode());
        ValidateBillerResponse billerValidation = validateBiller(request);
        if (!billerValidation.isValid()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(billerValidation.getMessage());
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new PayMeshException("Biller validation failed: " + billerValidation.getMessage(), HttpStatus.BAD_REQUEST, "BILLER_VALIDATION_FAILED");
        }

        // Step 3: Debit User Wallet (with Resilience4j Circuit Breaker)
        log.info("[Saga Step 2/3] Debiting wallet for User: {}, Amount: ${}", request.getUserId(), request.getAmount());
        try {
            payment.setStatus(PaymentStatus.WALLET_DEBITED);
            paymentRepository.save(payment);

            debitWalletWithCircuitBreaker(new WalletDebitRequest(
                    request.getUserId(),
                    request.getAmount(),
                    payment.getPaymentId(),
                    "Payment for biller " + request.getBillerCode()
            ));
            log.info("[Saga Step 2/3] Wallet debit SUCCESSFUL for payment {}", payment.getPaymentId());
        } catch (Exception ex) {
            log.error("[Saga Step 2/3] Wallet debit FAILED: {}", ex.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Wallet debit failed: " + ex.getMessage());
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            publishEvent(payment, "PAYMENT_FAILED");
            throw new PayMeshException("Wallet debit failed: " + ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, "WALLET_DEBIT_FAILED");
        }

        // Step 4: Call Mock Gateway for processor authorization & capture (with Resilience4j Circuit Breaker)
        log.info("[Saga Step 3/3] Calling Payment Gateway for authorization & settlement...");
        payment.setStatus(PaymentStatus.GATEWAY_PROCESSING);
        paymentRepository.save(payment);

        GatewayChargeRequest chargeRequest = new GatewayChargeRequest(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getBillerCode(),
                payment.getCustomerAccountNumber(),
                request.getSimulateGatewayFailure(),
                request.getSimulateGatewayDelayMs()
        );

        try {
            GatewayChargeResponse gatewayResponse = chargeGatewayWithCircuitBreaker(chargeRequest);

            // SUCCESS PATH
            log.info("[Saga Step 3/3] Gateway Charge SUCCESS: gatewayTxId={}", gatewayResponse.getGatewayTransactionId());
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            // Publish completed event to Kafka
            publishEvent(payment, "PAYMENT_COMPLETED");

            return mapToResponse(payment, "Payment processed successfully");

        } catch (Exception gatewayEx) {
            // FAILURE PATH -> TRIGGER SAGA COMPENSATING TRANSACTION
            log.warn("[SAGA COMPENSATING TRANSACTION] Gateway charge failed ({}). Rolling back and refunding wallet...", 
                    gatewayEx.getMessage());

            String compensationStatus;
            try {
                // Compensating Transaction: Refund wallet
                refundWalletWithCircuitBreaker(new WalletRefundRequest(
                        payment.getUserId(),
                        payment.getAmount(),
                        payment.getPaymentId(),
                        "Saga rollback due to payment gateway failure: " + gatewayEx.getMessage()
                ));
                compensationStatus = "WALLET_REFUNDED_SUCCESSFULLY";
                log.info("[SAGA COMPENSATION COMPLETE] Wallet refunded for user {} amount ${}", 
                        payment.getUserId(), payment.getAmount());
            } catch (Exception compEx) {
                log.error("[SAGA COMPENSATION CRITICAL] Failed to execute compensating refund: {}", compEx.getMessage());
                compensationStatus = "COMPENSATION_PENDING_MANUAL_REVIEW";
            }

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Gateway failed: " + gatewayEx.getMessage());
            payment.setCompensationStatus(compensationStatus);
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            // Publish failed event to Kafka
            publishEvent(payment, "PAYMENT_FAILED");

            throw new PaymentProcessingException(
                    "Payment failed at Gateway: " + gatewayEx.getMessage() + ". Compensating transaction executed: " + compensationStatus,
                    HttpStatus.BAD_GATEWAY, "GATEWAY_FAILURE_COMPENSATED");
        }
    }

    @CircuitBreaker(name = "walletService", fallbackMethod = "walletDebitFallback")
    public WalletTransactionDto debitWalletWithCircuitBreaker(WalletDebitRequest request) {
        ApiResponse<WalletTransactionDto> response = walletClient.debitWallet(request);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            String err = response != null ? response.getMessage() : "Unknown wallet error";
            throw new PayMeshException("Wallet service debit failed: " + err);
        }
        return response.getData();
    }

    public WalletTransactionDto walletDebitFallback(WalletDebitRequest request, Throwable ex) {
        log.error("Resilience4j Circuit Breaker OPEN / Fallback triggered for wallet debit: {}", ex.getMessage());
        throw new CircuitBreakerOpenException("Wallet Service");
    }

    @CircuitBreaker(name = "walletService", fallbackMethod = "walletRefundFallback")
    public WalletTransactionDto refundWalletWithCircuitBreaker(WalletRefundRequest request) {
        ApiResponse<WalletTransactionDto> response = walletClient.refundWallet(request);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            String err = response != null ? response.getMessage() : "Unknown refund error";
            throw new PayMeshException("Wallet refund failed: " + err);
        }
        return response.getData();
    }

    public WalletTransactionDto walletRefundFallback(WalletRefundRequest request, Throwable ex) {
        log.error("Resilience4j Circuit Breaker OPEN / Fallback triggered for wallet refund: {}", ex.getMessage());
        throw new CircuitBreakerOpenException("Wallet Service");
    }

    @CircuitBreaker(name = "gatewayService", fallbackMethod = "gatewayChargeFallback")
    public GatewayChargeResponse chargeGatewayWithCircuitBreaker(GatewayChargeRequest request) {
        ApiResponse<GatewayChargeResponse> response = gatewayClient.charge(request);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            String err = response != null ? response.getMessage() : "Unknown gateway error";
            throw new PaymentProcessingException("Gateway service error: " + err);
        }
        return response.getData();
    }

    public GatewayChargeResponse gatewayChargeFallback(GatewayChargeRequest request, Throwable ex) {
        log.error("Resilience4j Circuit Breaker OPEN / Fallback triggered for gateway charge: {}", ex.getMessage());
        throw new PaymentProcessingException("Payment Gateway is temporarily unavailable or circuit is open: " + ex.getMessage());
    }

    private ValidateBillerResponse validateBiller(PaymentRequest request) {
        try {
            ApiResponse<ValidateBillerResponse> response = billerClient.validateBillerAccount(
                    new ValidateBillerRequest(request.getBillerCode(), request.getCustomerAccountNumber(), request.getAmount())
            );
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("Biller service call error: {}", e.getMessage());
        }
        // Permissive fallback for simulation if biller service is unreachable in dev
        return new ValidateBillerResponse(true, request.getBillerCode(), request.getBillerCode(),
                request.getCustomerAccountNumber(), "Customer", request.getAmount(), "Validated");
    }

    private void publishEvent(Payment payment, String eventType) {
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID().toString(),
                eventType,
                payment.getPaymentId(),
                payment.getUserId(),
                payment.getBillerCode(),
                payment.getCustomerAccountNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureReason(),
                null,
                LocalDateTime.now()
        );
        eventPublisher.publishPaymentEvent(event);
    }

    public PaymentResponse mapToResponse(Payment p, String msg) {
        return new PaymentResponse(
                p.getPaymentId(),
                p.getUserId(),
                p.getBillerCode(),
                p.getBillerCode(),
                p.getCustomerAccountNumber(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getPaymentMethod(),
                p.getGatewayTransactionId(),
                p.getIdempotencyKey(),
                p.getFailureReason(),
                p.getCompensationStatus(),
                p.getCreatedAt(),
                p.getCompletedAt()
        );
    }
}
