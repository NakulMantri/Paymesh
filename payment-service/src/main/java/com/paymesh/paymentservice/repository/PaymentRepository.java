package com.paymesh.paymentservice.repository;

import com.paymesh.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
