package com.paymesh.billerservice.service;

import com.paymesh.common.dto.biller.BillerCategoryDto;
import com.paymesh.common.dto.biller.BillerDto;
import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;

import java.util.List;

public interface BillerService {
    List<BillerCategoryDto> getAllCategories();
    List<BillerDto> getAllBillers();
    List<BillerDto> getBillersByCategory(String categoryCode);
    BillerDto getBillerByCode(String billerCode);
    ValidateBillerResponse validateBillerAccount(ValidateBillerRequest request);
}
