package com.paymesh.paymentservice.saga;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.gateway.GatewayStatus;
import com.paymesh.common.dto.payment.PaymentMethod;
import com.paymesh.common.dto.payment.PaymentRequest;
import com.paymesh.common.dto.payment.PaymentResponse;
import com.paymesh.common.dto.payment.PaymentStatus;
import com.paymesh.common.dto.wallet.TransactionType;
import com.paymesh.common.dto.wallet.WalletDebitRequest;
import com.paymesh.common.dto.wallet.WalletRefundRequest;
import com.paymesh.common.dto.wallet.WalletTransactionDto;
import com.paymesh.common.exception.PaymentProcessingException;
import com.paymesh.paymentservice.client.BillerClient;
import com.paymesh.paymentservice.client.GatewayClient;
import com.paymesh.paymentservice.client.WalletClient;
import com.paymesh.paymentservice.entity.Payment;
import com.paymesh.paymentservice.event.PaymentEventPublisher;
import com.paymesh.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSagaOrchestratorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private BillerClient billerClient;

    @Mock
    private GatewayClient gatewayClient;

    @Mock
    private PaymentEventPublisher eventPublisher;

    private PaymentSagaOrchestrator sagaOrchestrator;

    @BeforeEach
    void setUp() {
        sagaOrchestrator = new PaymentSagaOrchestrator(
                paymentRepository,
                walletClient,
                billerClient,
                gatewayClient,
                eventPublisher
        );
    }

    @Test
    void testPaymentSagaSuccess() {
        PaymentRequest request = new PaymentRequest(
                1L, "ELEC-CONED", "1234567890", new BigDecimal("120.00"), "USD",
                PaymentMethod.WALLET, "IDEMP-100", false, 0
        );

        when(paymentRepository.findByIdempotencyKey("IDEMP-100")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Mock Biller Client
        ValidateBillerResponse valResp = new ValidateBillerResponse(true, "ELEC-CONED", "Con Edison", "1234567890", "John Doe", new BigDecimal("120.00"), "Valid");
        when(billerClient.validateBillerAccount(any())).thenReturn(ApiResponse.ok(valResp));

        // Mock Wallet Client
        WalletTransactionDto debitTx = new WalletTransactionDto(1L, 10L, 1L, TransactionType.DEBIT, new BigDecimal("120.00"), new BigDecimal("880.00"), "PAY-REF", "Payment", LocalDateTime.now());
        when(walletClient.debitWallet(any(WalletDebitRequest.class))).thenReturn(ApiResponse.ok(debitTx));

        // Mock Gateway Client
        GatewayChargeResponse chargeResp = new GatewayChargeResponse("GW-12345", "PAY-REF", GatewayStatus.SUCCESS, new BigDecimal("120.00"), "GW_200", "Approved", LocalDateTime.now());
        when(gatewayClient.charge(any(GatewayChargeRequest.class))).thenReturn(ApiResponse.ok(chargeResp));

        PaymentResponse response = sagaOrchestrator.executePaymentSaga(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertEquals("GW-12345", response.getGatewayTransactionId());
        verify(eventPublisher, times(1)).publishPaymentEvent(any());
    }

    @Test
    void testPaymentSagaGatewayFailureTriggersCompensatingRefund() {
        PaymentRequest request = new PaymentRequest(
                1L, "ELEC-CONED", "1234567890", new BigDecimal("120.00"), "USD",
                PaymentMethod.WALLET, "IDEMP-200", true, 0
        );

        when(paymentRepository.findByIdempotencyKey("IDEMP-200")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Mock Biller Client
        ValidateBillerResponse valResp = new ValidateBillerResponse(true, "ELEC-CONED", "Con Edison", "1234567890", "John Doe", new BigDecimal("120.00"), "Valid");
        when(billerClient.validateBillerAccount(any())).thenReturn(ApiResponse.ok(valResp));

        // Mock Wallet Client Debit Success
        WalletTransactionDto debitTx = new WalletTransactionDto(1L, 10L, 1L, TransactionType.DEBIT, new BigDecimal("120.00"), new BigDecimal("880.00"), "PAY-REF", "Payment", LocalDateTime.now());
        when(walletClient.debitWallet(any(WalletDebitRequest.class))).thenReturn(ApiResponse.ok(debitTx));

        // Mock Gateway Client Failure
        when(gatewayClient.charge(any(GatewayChargeRequest.class))).thenThrow(new RuntimeException("Simulated 3rd party gateway timeout"));

        // Mock Wallet Compensating Refund
        WalletTransactionDto refundTx = new WalletTransactionDto(2L, 10L, 1L, TransactionType.REFUND, new BigDecimal("120.00"), new BigDecimal("1000.00"), "REFUND-PAY-REF", "Refund", LocalDateTime.now());
        when(walletClient.refundWallet(any(WalletRefundRequest.class))).thenReturn(ApiResponse.ok(refundTx));

        assertThrows(PaymentProcessingException.class, () -> sagaOrchestrator.executePaymentSaga(request));

        // Verify that wallet refund compensating transaction was triggered!
        verify(walletClient, times(1)).refundWallet(any(WalletRefundRequest.class));
        verify(eventPublisher, times(1)).publishPaymentEvent(any());
    }
}
