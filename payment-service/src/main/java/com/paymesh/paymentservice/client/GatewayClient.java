package com.paymesh.paymentservice.client;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.gateway.GatewayRefundRequest;
import com.paymesh.common.dto.gateway.GatewayRefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mock-gateway-service")
public interface GatewayClient {

    @PostMapping("/api/v1/mock-gateway/charge")
    ApiResponse<GatewayChargeResponse> charge(@RequestBody GatewayChargeRequest request);

    @PostMapping("/api/v1/mock-gateway/refund")
    ApiResponse<GatewayRefundResponse> refund(@RequestBody GatewayRefundRequest request);
}
