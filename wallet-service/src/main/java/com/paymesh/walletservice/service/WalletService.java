package com.paymesh.walletservice.service;

import com.paymesh.common.dto.wallet.*;
import java.util.List;

public interface WalletService {
    WalletDto getWalletByUserId(Long userId);
    WalletTransactionDto credit(WalletCreditRequest request);
    WalletTransactionDto debit(WalletDebitRequest request);
    WalletTransactionDto refund(WalletRefundRequest request);
    List<WalletTransactionDto> getTransactionsByUserId(Long userId);
}
