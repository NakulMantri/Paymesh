package com.paymesh.paymentservice.client;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.wallet.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @GetMapping("/api/v1/wallets/user/{userId}")
    ApiResponse<WalletDto> getWalletByUserId(@PathVariable("userId") Long userId);

    @PostMapping("/api/v1/wallets/debit")
    ApiResponse<WalletTransactionDto> debitWallet(@RequestBody WalletDebitRequest request);

    @PostMapping("/api/v1/wallets/refund")
    ApiResponse<WalletTransactionDto> refundWallet(@RequestBody WalletRefundRequest request);
}
