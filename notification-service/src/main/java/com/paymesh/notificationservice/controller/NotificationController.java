package com.paymesh.notificationservice.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.notificationservice.entity.NotificationLog;
import com.paymesh.notificationservice.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getRecentLogs() {
        List<NotificationLog> logs = notificationRepository.findTop50ByOrderBySentAtDesc();
        return ResponseEntity.ok(ApiResponse.ok("Recent notification logs", logs));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getLogsByUserId(@PathVariable Long userId) {
        List<NotificationLog> logs = notificationRepository.findByUserIdOrderBySentAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getLogsByPaymentId(@PathVariable String paymentId) {
        List<NotificationLog> logs = notificationRepository.findByPaymentIdOrderBySentAtDesc(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
