package com.paymesh.mockgateway.repository;

import com.paymesh.mockgateway.entity.MockGatewayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MockGatewayRepository extends JpaRepository<MockGatewayTransaction, Long> {
    Optional<MockGatewayTransaction> findByGatewayTransactionId(String gatewayTransactionId);
    Optional<MockGatewayTransaction> findByPaymentReference(String paymentReference);
}
