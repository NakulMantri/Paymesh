package com.paymesh.common.dto.biller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ValidateBillerRequest {
    @NotBlank(message = "Biller code is required")
    private String billerCode;

    @NotBlank(message = "Customer account number is required")
    private String customerAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    public ValidateBillerRequest() {}

    public ValidateBillerRequest(String billerCode, String customerAccountNumber, BigDecimal amount) {
        this.billerCode = billerCode;
        this.customerAccountNumber = customerAccountNumber;
        this.amount = amount;
    }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
