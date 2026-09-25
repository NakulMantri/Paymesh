package com.paymesh.mockgateway.service;

import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.gateway.GatewayStatus;
import com.paymesh.common.exception.PaymentProcessingException;
import com.paymesh.mockgateway.entity.MockGatewayTransaction;
import com.paymesh.mockgateway.repository.MockGatewayRepository;
import com.paymesh.mockgateway.service.impl.MockGatewayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockGatewayServiceTest {

    @Mock
    private MockGatewayRepository repository;

    private MockGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        gatewayService = new MockGatewayServiceImpl(repository, 0, 0);
    }

    @Test
    void testProcessChargeSuccess() {
        GatewayChargeRequest request = new GatewayChargeRequest(
                "PAY-REF-1", new BigDecimal("50.00"), "USD", "ELEC-CONED", "1234567890", false, 0);

        when(repository.findByPaymentReference("PAY-REF-1")).thenReturn(Optional.empty());
        when(repository.save(any(MockGatewayTransaction.class))).thenAnswer(i -> i.getArgument(0));

        GatewayChargeResponse response = gatewayService.processCharge(request);

        assertNotNull(response);
        assertEquals(GatewayStatus.SUCCESS, response.getStatus());
        assertEquals("PAY-REF-1", response.getPaymentReference());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
    }

    @Test
    void testProcessChargeSimulatedFailure() {
        GatewayChargeRequest request = new GatewayChargeRequest(
                "PAY-REF-2", new BigDecimal("50.00"), "USD", "ELEC-CONED", "1234567890", true, 0);

        when(repository.findByPaymentReference("PAY-REF-2")).thenReturn(Optional.empty());

        assertThrows(PaymentProcessingException.class, () -> gatewayService.processCharge(request));
    }
}
