package com.paymesh.walletservice.repository;

import com.paymesh.walletservice.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<WalletTransaction> findByReferenceId(String referenceId);
    boolean existsByReferenceId(String referenceId);
}
