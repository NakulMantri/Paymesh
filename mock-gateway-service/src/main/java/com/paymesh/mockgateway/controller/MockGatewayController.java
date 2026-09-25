package com.paymesh.mockgateway.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.gateway.GatewayChargeRequest;
import com.paymesh.common.dto.gateway.GatewayChargeResponse;
import com.paymesh.common.dto.gateway.GatewayRefundRequest;
import com.paymesh.common.dto.gateway.GatewayRefundResponse;
import com.paymesh.mockgateway.service.MockGatewayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mock-gateway")
public class MockGatewayController {

    private final MockGatewayService gatewayService;

    public MockGatewayController(MockGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<GatewayChargeResponse>> processCharge(
            @Valid @RequestBody GatewayChargeRequest request) {
        GatewayChargeResponse response = gatewayService.processCharge(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment authorized and charged by processor", response));
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<GatewayRefundResponse>> processRefund(
            @Valid @RequestBody GatewayRefundRequest request) {
        GatewayRefundResponse response = gatewayService.processRefund(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded by processor", response));
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        return ResponseEntity.ok(ApiResponse.ok("Current simulation configuration", gatewayService.getConfig()));
    }

    @PostMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateConfig(@RequestBody Map<String, Integer> config) {
        int failureRate = config.getOrDefault("failureRatePercent", 0);
        int delayMs = config.getOrDefault("delayMs", 50);
        Map<String, Object> updated = gatewayService.updateConfig(failureRate, delayMs);
        return ResponseEntity.ok(ApiResponse.ok("Updated simulation configuration", updated));
    }
}
