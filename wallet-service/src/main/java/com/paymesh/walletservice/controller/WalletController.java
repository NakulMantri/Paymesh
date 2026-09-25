package com.paymesh.walletservice.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.wallet.*;
import com.paymesh.common.util.SecurityConstants;
import com.paymesh.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletDto>> getMyWallet(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Missing X-User-Id header", "BAD_REQUEST"));
        }
        WalletDto wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(wallet));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WalletDto>> getWalletByUserId(@PathVariable Long userId) {
        WalletDto wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(wallet));
    }

    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<WalletTransactionDto>> creditWallet(@Valid @RequestBody WalletCreditRequest request) {
        WalletTransactionDto tx = walletService.credit(request);
        return ResponseEntity.ok(ApiResponse.ok("Wallet credited successfully", tx));
    }

    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<WalletTransactionDto>> debitWallet(@Valid @RequestBody WalletDebitRequest request) {
        WalletTransactionDto tx = walletService.debit(request);
        return ResponseEntity.ok(ApiResponse.ok("Wallet debited successfully", tx));
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<WalletTransactionDto>> refundWallet(@Valid @RequestBody WalletRefundRequest request) {
        WalletTransactionDto tx = walletService.refund(request);
        return ResponseEntity.ok(ApiResponse.ok("Wallet refunded successfully (Saga compensation)", tx));
    }

    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto>>> getTransactions(@PathVariable Long userId) {
        List<WalletTransactionDto> transactions = walletService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
