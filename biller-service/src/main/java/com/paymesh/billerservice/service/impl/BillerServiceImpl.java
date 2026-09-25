package com.paymesh.billerservice.service.impl;

import com.paymesh.common.dto.biller.BillerCategoryDto;
import com.paymesh.common.dto.biller.BillerDto;
import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import com.paymesh.common.exception.PayMeshException;
import com.paymesh.common.exception.ResourceNotFoundException;
import com.paymesh.billerservice.entity.Biller;
import com.paymesh.billerservice.entity.BillerCategory;
import com.paymesh.billerservice.repository.BillerCategoryRepository;
import com.paymesh.billerservice.repository.BillerRepository;
import com.paymesh.billerservice.service.BillerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillerServiceImpl implements BillerService {

    private static final Logger log = LoggerFactory.getLogger(BillerServiceImpl.class);

    private final BillerRepository billerRepository;
    private final BillerCategoryRepository categoryRepository;

    public BillerServiceImpl(BillerRepository billerRepository, BillerCategoryRepository categoryRepository) {
        this.billerRepository = billerRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void seedInitialData() {
        if (categoryRepository.count() == 0) {
            log.info("Seeding initial biller categories and billers...");

            BillerCategory electricity = categoryRepository.save(new BillerCategory(
                    "ELECTRICITY", "Electricity & Power", "Grid power and clean energy utility bills", "bolt"));
            BillerCategory water = categoryRepository.save(new BillerCategory(
                    "WATER", "Water & Sewage", "Municipal water supply and drainage bills", "water_drop"));
            BillerCategory internet = categoryRepository.save(new BillerCategory(
                    "INTERNET", "Broadband & Fiber", "High-speed internet and fiber optic subscriptions", "wifi"));
            BillerCategory mobile = categoryRepository.save(new BillerCategory(
                    "MOBILE", "Mobile & Telecom", "Postpaid cellular and data plan billing", "phone_iphone"));
            BillerCategory gas = categoryRepository.save(new BillerCategory(
                    "GAS", "Natural Gas", "Pipeline natural gas utilities", "local_fire_department"));

            // Seed Billers
            billerRepository.save(new Biller("ELEC-CONED", "Con Edison Power", electricity, "^[0-9]{10}$",
                    new BigDecimal("5.00"), new BigDecimal("5000.00"), "support@coned.com", "+1-800-555-0101"));
            billerRepository.save(new Biller("ELEC-PGE", "Pacific Gas & Electric", electricity, "^[0-9]{10}$",
                    new BigDecimal("5.00"), new BigDecimal("5000.00"), "support@pge.com", "+1-800-555-0102"));
            billerRepository.save(new Biller("WATER-NYC", "NYC Environmental Protection Water", water, "^[0-9]{8,12}$",
                    new BigDecimal("2.00"), new BigDecimal("3000.00"), "billing@dep.nyc.gov", "+1-800-555-0201"));
            billerRepository.save(new Biller("NET-VERIZON", "Verizon Fios Fiber", internet, "^[0-9]{10}$",
                    new BigDecimal("10.00"), new BigDecimal("2000.00"), "care@verizon.com", "+1-800-555-0301"));
            billerRepository.save(new Biller("MOBI-ATT", "AT&T Mobility Wireless", mobile, "^[0-9]{10}$",
                    new BigDecimal("10.00"), new BigDecimal("1500.00"), "support@att.com", "+1-800-555-0401"));
            billerRepository.save(new Biller("GAS-NATGRID", "National Grid Gas Utility", gas, "^[0-9]{10}$",
                    new BigDecimal("5.00"), new BigDecimal("4000.00"), "service@nationalgrid.com", "+1-800-555-0501"));

            log.info("Finished seeding billers successfully.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillerCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillerDto> getAllBillers() {
        return billerRepository.findByActiveTrue().stream()
                .map(this::mapBillerToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillerDto> getBillersByCategory(String categoryCode) {
        return billerRepository.findByCategoryCodeAndActiveTrue(categoryCode.toUpperCase()).stream()
                .map(this::mapBillerToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BillerDto getBillerByCode(String billerCode) {
        Biller biller = billerRepository.findByBillerCode(billerCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Biller", "billerCode", billerCode));
        return mapBillerToDto(biller);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateBillerResponse validateBillerAccount(ValidateBillerRequest request) {
        Biller biller = billerRepository.findByBillerCode(request.getBillerCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Biller", "billerCode", request.getBillerCode()));

        if (!biller.isActive()) {
            throw new PayMeshException("Biller is currently inactive", HttpStatus.BAD_REQUEST, "BILLER_INACTIVE");
        }

        // Validate account regex
        if (biller.getAccountNumberPattern() != null && !biller.getAccountNumberPattern().isBlank()) {
            boolean matches = Pattern.matches(biller.getAccountNumberPattern(), request.getCustomerAccountNumber());
            if (!matches) {
                return new ValidateBillerResponse(
                        false,
                        biller.getBillerCode(),
                        biller.getName(),
                        request.getCustomerAccountNumber(),
                        null,
                        null,
                        "Invalid account number format for " + biller.getName() + ". Expected format pattern: " + biller.getAccountNumberPattern()
                );
            }
        }

        // Validate amount constraints
        if (request.getAmount().compareTo(biller.getMinAmount()) < 0) {
            return new ValidateBillerResponse(
                    false,
                    biller.getBillerCode(),
                    biller.getName(),
                    request.getCustomerAccountNumber(),
                    null,
                    null,
                    "Payment amount is below the minimum allowed limit of $" + biller.getMinAmount()
            );
        }

        if (request.getAmount().compareTo(biller.getMaxAmount()) > 0) {
            return new ValidateBillerResponse(
                    false,
                    biller.getBillerCode(),
                    biller.getName(),
                    request.getCustomerAccountNumber(),
                    null,
                    null,
                    "Payment amount exceeds the maximum allowed limit of $" + biller.getMaxAmount()
            );
        }

        return new ValidateBillerResponse(
                true,
                biller.getBillerCode(),
                biller.getName(),
                request.getCustomerAccountNumber(),
                "Verified Customer (" + request.getCustomerAccountNumber() + ")",
                request.getAmount(),
                "Account validated successfully"
        );
    }

    private BillerCategoryDto mapCategoryToDto(BillerCategory c) {
        return new BillerCategoryDto(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDescription(),
                c.getIconUrl(),
                c.isActive()
        );
    }

    private BillerDto mapBillerToDto(Biller b) {
        return new BillerDto(
                b.getId(),
                b.getBillerCode(),
                b.getName(),
                b.getCategory().getId(),
                b.getCategory().getName(),
                b.getAccountNumberPattern(),
                b.getMinAmount(),
                b.getMaxAmount(),
                b.isActive(),
                b.getContactEmail(),
                b.getContactPhone(),
                b.getCreatedAt()
        );
    }
}
