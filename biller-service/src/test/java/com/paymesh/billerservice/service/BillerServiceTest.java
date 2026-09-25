package com.paymesh.billerservice.service;

import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import com.paymesh.billerservice.entity.Biller;
import com.paymesh.billerservice.entity.BillerCategory;
import com.paymesh.billerservice.repository.BillerCategoryRepository;
import com.paymesh.billerservice.repository.BillerRepository;
import com.paymesh.billerservice.service.impl.BillerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillerServiceTest {

    @Mock
    private BillerRepository billerRepository;

    @Mock
    private BillerCategoryRepository categoryRepository;

    private BillerService billerService;

    @BeforeEach
    void setUp() {
        billerService = new BillerServiceImpl(billerRepository, categoryRepository);
    }

    @Test
    void testValidateBillerSuccess() {
        BillerCategory cat = new BillerCategory("ELECTRICITY", "Electricity", "Power", "bolt");
        Biller biller = new Biller("ELEC-CONED", "Con Edison", cat, "^[0-9]{10}$", new BigDecimal("5.00"), new BigDecimal("5000.00"), "test@coned.com", "12345");

        when(billerRepository.findByBillerCode("ELEC-CONED")).thenReturn(Optional.of(biller));

        ValidateBillerRequest request = new ValidateBillerRequest("ELEC-CONED", "1234567890", new BigDecimal("150.00"));
        ValidateBillerResponse response = billerService.validateBillerAccount(request);

        assertTrue(response.isValid());
        assertEquals("ELEC-CONED", response.getBillerCode());
    }

    @Test
    void testValidateBillerInvalidAccountNumber() {
        BillerCategory cat = new BillerCategory("ELECTRICITY", "Electricity", "Power", "bolt");
        Biller biller = new Biller("ELEC-CONED", "Con Edison", cat, "^[0-9]{10}$", new BigDecimal("5.00"), new BigDecimal("5000.00"), "test@coned.com", "12345");

        when(billerRepository.findByBillerCode("ELEC-CONED")).thenReturn(Optional.of(biller));

        ValidateBillerRequest request = new ValidateBillerRequest("ELEC-CONED", "INVALID-ACC", new BigDecimal("150.00"));
        ValidateBillerResponse response = billerService.validateBillerAccount(request);

        assertFalse(response.isValid());
    }
}
