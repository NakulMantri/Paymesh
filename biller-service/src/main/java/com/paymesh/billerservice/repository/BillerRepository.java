package com.paymesh.billerservice.repository;

import com.paymesh.billerservice.entity.Biller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillerRepository extends JpaRepository<Biller, Long> {
    Optional<Biller> findByBillerCode(String billerCode);
    List<Biller> findByCategoryCodeAndActiveTrue(String categoryCode);
    List<Biller> findByActiveTrue();
    boolean existsByBillerCode(String billerCode);
}
