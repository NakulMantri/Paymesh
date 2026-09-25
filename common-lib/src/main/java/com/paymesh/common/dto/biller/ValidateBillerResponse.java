package com.paymesh.common.dto.biller;

import java.math.BigDecimal;

public class ValidateBillerResponse {
    private boolean valid;
    private String billerCode;
    private String billerName;
    private String customerAccountNumber;
    private String customerName;
    private BigDecimal dueAmount;
    private String message;

    public ValidateBillerResponse() {}

    public ValidateBillerResponse(boolean valid, String billerCode, String billerName, String customerAccountNumber, String customerName, BigDecimal dueAmount, String message) {
        this.valid = valid;
        this.billerCode = billerCode;
        this.billerName = billerName;
        this.customerAccountNumber = customerAccountNumber;
        this.customerName = customerName;
        this.dueAmount = dueAmount;
        this.message = message;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getBillerName() { return billerName; }
    public void setBillerName(String billerName) { this.billerName = billerName; }

    public String getCustomerAccountNumber() { return customerAccountNumber; }
    public void setCustomerAccountNumber(String customerAccountNumber) { this.customerAccountNumber = customerAccountNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
