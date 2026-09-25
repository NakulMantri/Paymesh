package com.paymesh.common.dto.biller;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillerDto {
    private Long id;
    private String billerCode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String accountNumberPattern;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private boolean active;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;

    public BillerDto() {}

    public BillerDto(Long id, String billerCode, String name, Long categoryId, String categoryName, String accountNumberPattern, BigDecimal minAmount, BigDecimal maxAmount, boolean active, String contactEmail, String contactPhone, LocalDateTime createdAt) {
        this.id = id;
        this.billerCode = billerCode;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.accountNumberPattern = accountNumberPattern;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.active = active;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getAccountNumberPattern() { return accountNumberPattern; }
    public void setAccountNumberPattern(String accountNumberPattern) { this.accountNumberPattern = accountNumberPattern; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
