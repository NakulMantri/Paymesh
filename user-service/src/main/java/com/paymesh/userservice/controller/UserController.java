package com.paymesh.userservice.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.auth.UserDto;
import com.paymesh.common.util.SecurityConstants;
import com.paymesh.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Missing X-User-Id header", "BAD_REQUEST"));
        }
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) Long userId,
            @RequestBody Map<String, String> payload) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Missing X-User-Id header", "BAD_REQUEST"));
        }
        String fullName = payload.get("fullName");
        String phoneNumber = payload.get("phoneNumber");
        UserDto updated = userService.updateUserProfile(userId, fullName, phoneNumber);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }
}
