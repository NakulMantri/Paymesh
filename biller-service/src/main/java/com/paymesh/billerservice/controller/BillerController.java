package com.paymesh.billerservice.controller;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.biller.BillerCategoryDto;
import com.paymesh.common.dto.biller.BillerDto;
import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import com.paymesh.billerservice.service.BillerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billers")
public class BillerController {

    private final BillerService billerService;

    public BillerController(BillerService billerService) {
        this.billerService = billerService;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BillerCategoryDto>>> getCategories() {
        List<BillerCategoryDto> categories = billerService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillerDto>>> getAllBillers() {
        List<BillerDto> billers = billerService.getAllBillers();
        return ResponseEntity.ok(ApiResponse.ok(billers));
    }

    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<List<BillerDto>>> getBillersByCategory(@PathVariable String categoryCode) {
        List<BillerDto> billers = billerService.getBillersByCategory(categoryCode);
        return ResponseEntity.ok(ApiResponse.ok(billers));
    }

    @GetMapping("/{billerCode}")
    public ResponseEntity<ApiResponse<BillerDto>> getBillerByCode(@PathVariable String billerCode) {
        BillerDto biller = billerService.getBillerByCode(billerCode);
        return ResponseEntity.ok(ApiResponse.ok(biller));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateBillerResponse>> validateBillerAccount(
            @Valid @RequestBody ValidateBillerRequest request) {
        ValidateBillerResponse response = billerService.validateBillerAccount(request);
        return ResponseEntity.ok(ApiResponse.ok("Biller account validation completed", response));
    }
}
