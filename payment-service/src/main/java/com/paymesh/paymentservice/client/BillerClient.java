package com.paymesh.paymentservice.client;

import com.paymesh.common.dto.ApiResponse;
import com.paymesh.common.dto.biller.BillerDto;
import com.paymesh.common.dto.biller.ValidateBillerRequest;
import com.paymesh.common.dto.biller.ValidateBillerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "biller-service")
public interface BillerClient {

    @GetMapping("/api/v1/billers/{billerCode}")
    ApiResponse<BillerDto> getBillerByCode(@PathVariable("billerCode") String billerCode);

    @PostMapping("/api/v1/billers/validate")
    ApiResponse<ValidateBillerResponse> validateBillerAccount(@RequestBody ValidateBillerRequest request);
}
