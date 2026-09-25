package com.paymesh.billerservice.repository;

import com.paymesh.billerservice.entity.BillerCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillerCategoryRepository extends JpaRepository<BillerCategory, Long> {
    Optional<BillerCategory> findByCode(String code);
    boolean existsByCode(String code);
}
