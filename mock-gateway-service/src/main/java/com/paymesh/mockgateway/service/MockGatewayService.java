package com.paymesh.mockgateway.service;

import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.gateway.GatewayRefundRequest;
import com.paymesh.common.dto.gateway.GatewayRefundResponse;

import java.util.Map;

public interface MockGatewayService {
    GatewayChargeResponse processCharge(GatewayChargeRequest request);
    GatewayRefundResponse processRefund(GatewayRefundRequest request);
    Map<String, Object> getConfig();
    Map<String, Object> updateConfig(int failureRatePercent, int delayMs);
}
