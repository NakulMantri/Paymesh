package com.paymesh.walletservice.service;

import com.paymesh.common.dto.wallet.TransactionType;
import com.paymesh.common.dto.wallet.WalletCreditRequest;
import com.paymesh.common.dto.wallet.WalletDebitRequest;
import com.paymesh.common.dto.wallet.WalletRefundRequest;
import com.paymesh.common.dto.wallet.WalletTransactionDto;
import com.paymesh.common.exception.InsufficientBalanceException;
import com.paymesh.walletservice.entity.Wallet;
import com.paymesh.walletservice.entity.WalletTransaction;
import com.paymesh.walletservice.repository.WalletRepository;
import com.paymesh.walletservice.repository.WalletTransactionRepository;
import com.paymesh.walletservice.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(walletRepository, transactionRepository);
    }

    @Test
    void testDebitSuccess() {
        Long userId = 1L;
        Wallet wallet = new Wallet(userId, new BigDecimal("500.00"), "USD");
        wallet.setId(10L);

        when(transactionRepository.findByReferenceId("PAY-100")).thenReturn(Optional.empty());
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        WalletTransaction tx = new WalletTransaction(10L, userId, TransactionType.DEBIT, new BigDecimal("100.00"), new BigDecimal("400.00"), "PAY-100", "Payment");
        tx.setId(1L);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(tx);

        WalletDebitRequest debitReq = new WalletDebitRequest(userId, new BigDecimal("100.00"), "PAY-100", "Payment");
        WalletTransactionDto result = walletService.debit(debitReq);

        assertNotNull(result);
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }

    @Test
    void testDebitInsufficientBalance() {
        Long userId = 1L;
        Wallet wallet = new Wallet(userId, new BigDecimal("50.00"), "USD");
        wallet.setId(10L);

        when(transactionRepository.findByReferenceId("PAY-100")).thenReturn(Optional.empty());
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletDebitRequest debitReq = new WalletDebitRequest(userId, new BigDecimal("100.00"), "PAY-100", "Payment");
        assertThrows(InsufficientBalanceException.class, () -> walletService.debit(debitReq));
    }

    @Test
    void testRefundSuccess() {
        Long userId = 1L;
        Wallet wallet = new Wallet(userId, new BigDecimal("400.00"), "USD");
        wallet.setId(10L);

        when(transactionRepository.findByReferenceId("REFUND-PAY-100")).thenReturn(Optional.empty());
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        WalletTransaction tx = new WalletTransaction(10L, userId, TransactionType.REFUND, new BigDecimal("100.00"), new BigDecimal("500.00"), "REFUND-PAY-100", "Compensating refund");
        tx.setId(2L);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(tx);

        WalletRefundRequest refundReq = new WalletRefundRequest(userId, new BigDecimal("100.00"), "PAY-100", "Compensating refund");
        WalletTransactionDto result = walletService.refund(refundReq);

        assertNotNull(result);
        assertEquals(TransactionType.REFUND, result.getType());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }
}
