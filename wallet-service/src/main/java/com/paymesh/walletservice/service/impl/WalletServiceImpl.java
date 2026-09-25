package com.paymesh.walletservice.service.impl;

import com.paymesh.common.dto.wallet.*;
import com.paymesh.common.exception.InsufficientBalanceException;
import com.paymesh.common.exception.OptimisticLockingException;
import com.paymesh.common.exception.ResourceNotFoundException;
import com.paymesh.walletservice.entity.Wallet;
import com.paymesh.walletservice.entity.WalletTransaction;
import com.paymesh.walletservice.repository.WalletRepository;
import com.paymesh.walletservice.repository.WalletTransactionRepository;
import com.paymesh.walletservice.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public WalletDto getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating initial wallet for user ID: {} with balance $1,000.00", userId);
                    Wallet newWallet = new Wallet(userId, new BigDecimal("1000.0000"), "USD");
                    Wallet saved = walletRepository.save(newWallet);
                    
                    // Add initial funding transaction
                    WalletTransaction initialTx = new WalletTransaction(
                            saved.getId(),
                            userId,
                            TransactionType.CREDIT,
                            new BigDecimal("1000.0000"),
                            saved.getBalance(),
                            "INIT-CREDIT-" + userId,
                            "Initial account welcome credit"
                    );
                    transactionRepository.save(initialTx);
                    return saved;
                });
        return mapToDto(wallet);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletTransactionDto credit(WalletCreditRequest request) {
        log.info("Processing wallet credit for user: {}, amount: {}, ref: {}", 
                request.getUserId(), request.getAmount(), request.getReferenceId());

        // Check if already processed (Idempotency)
        Optional<WalletTransaction> existingTx = transactionRepository.findByReferenceId(request.getReferenceId());
        if (existingTx.isPresent()) {
            log.info("Transaction ref {} already processed, returning existing record", request.getReferenceId());
            return mapTransactionToDto(existingTx.get());
        }

        try {
            Wallet wallet = walletRepository.findByUserId(request.getUserId())
                    .orElseGet(() -> walletRepository.save(new Wallet(request.getUserId(), BigDecimal.ZERO, "USD")));

            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            Wallet updatedWallet = walletRepository.save(wallet);

            WalletTransaction transaction = new WalletTransaction(
                    updatedWallet.getId(),
                    request.getUserId(),
                    TransactionType.CREDIT,
                    request.getAmount(),
                    updatedWallet.getBalance(),
                    request.getReferenceId(),
                    request.getDescription() != null ? request.getDescription() : "Wallet credit"
            );
            WalletTransaction savedTx = transactionRepository.save(transaction);
            return mapTransactionToDto(savedTx);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict during credit for user {}", request.getUserId());
            throw new OptimisticLockingException("Wallet concurrent update conflict. Please retry.");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletTransactionDto debit(WalletDebitRequest request) {
        log.info("Processing wallet debit for user: {}, amount: {}, ref: {}", 
                request.getUserId(), request.getAmount(), request.getReferenceId());

        // Check if already processed (Idempotency)
        Optional<WalletTransaction> existingTx = transactionRepository.findByReferenceId(request.getReferenceId());
        if (existingTx.isPresent()) {
            log.info("Debit transaction ref {} already processed, returning existing record", request.getReferenceId());
            return mapTransactionToDto(existingTx.get());
        }

        try {
            Wallet wallet = walletRepository.findByUserId(request.getUserId())
                    .orElseGet(() -> {
                        // Auto-provision initial wallet if absent
                        Wallet newWallet = new Wallet(request.getUserId(), new BigDecimal("1000.0000"), "USD");
                        return walletRepository.save(newWallet);
                    });

            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                log.warn("Insufficient funds for user {}: balance={}, requested={}", 
                        request.getUserId(), wallet.getBalance(), request.getAmount());
                throw new InsufficientBalanceException(String.format(
                        "Insufficient wallet balance. Current: $%s, Required: $%s",
                        wallet.getBalance().setScale(2), request.getAmount().setScale(2)));
            }

            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            Wallet updatedWallet = walletRepository.save(wallet);

            WalletTransaction transaction = new WalletTransaction(
                    updatedWallet.getId(),
                    request.getUserId(),
                    TransactionType.DEBIT,
                    request.getAmount(),
                    updatedWallet.getBalance(),
                    request.getReferenceId(),
                    request.getDescription() != null ? request.getDescription() : "Payment debit"
            );
            WalletTransaction savedTx = transactionRepository.save(transaction);
            return mapTransactionToDto(savedTx);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict during debit for user {}", request.getUserId());
            throw new OptimisticLockingException("Wallet concurrent update conflict. Please retry.");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletTransactionDto refund(WalletRefundRequest request) {
        log.info("Processing wallet refund (Saga compensation) for user: {}, amount: {}, originalRef: {}", 
                request.getUserId(), request.getAmount(), request.getOriginalReferenceId());

        String refundRef = "REFUND-" + request.getOriginalReferenceId();
        Optional<WalletTransaction> existingRefund = transactionRepository.findByReferenceId(refundRef);
        if (existingRefund.isPresent()) {
            log.info("Refund {} already processed, returning existing record", refundRef);
            return mapTransactionToDto(existingRefund.get());
        }

        try {
            Wallet wallet = walletRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", request.getUserId()));

            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            Wallet updatedWallet = walletRepository.save(wallet);

            WalletTransaction transaction = new WalletTransaction(
                    updatedWallet.getId(),
                    request.getUserId(),
                    TransactionType.REFUND,
                    request.getAmount(),
                    updatedWallet.getBalance(),
                    refundRef,
                    "Compensating transaction refund: " + (request.getReason() != null ? request.getReason() : "Payment failure")
            );
            WalletTransaction savedTx = transactionRepository.save(transaction);
            return mapTransactionToDto(savedTx);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict during refund for user {}", request.getUserId());
            throw new OptimisticLockingException("Wallet concurrent update conflict during refund.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionDto> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
    }

    private WalletDto mapToDto(Wallet wallet) {
        return new WalletDto(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getVersion(),
                wallet.getStatus(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    private WalletTransactionDto mapTransactionToDto(WalletTransaction tx) {
        return new WalletTransactionDto(
                tx.getId(),
                tx.getWalletId(),
                tx.getUserId(),
                tx.getType(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getReferenceId(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
