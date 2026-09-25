package com.paymesh.billerservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billers", indexes = {
        @Index(name = "idx_biller_code", columnList = "billerCode")
})
public class Biller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String billerCode;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private BillerCategory category;

    @Column(length = 100)
    private String accountNumberPattern; // e.g. "^[0-9]{10}$"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 100)
    private String contactEmail;

    @Column(length = 30)
    private String contactPhone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Biller() {}

    public Biller(String billerCode, String name, BillerCategory category, String accountNumberPattern, BigDecimal minAmount, BigDecimal maxAmount, String contactEmail, String contactPhone) {
        this.billerCode = billerCode;
        this.name = name;
        this.category = category;
        this.accountNumberPattern = accountNumberPattern;
        this.minAmount = minAmount != null ? minAmount : new BigDecimal("1.00");
        this.maxAmount = maxAmount != null ? maxAmount : new BigDecimal("10000.00");
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillerCode() { return billerCode; }
    public void setBillerCode(String billerCode) { this.billerCode = billerCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BillerCategory getCategory() { return category; }
    public void setCategory(BillerCategory category) { this.category = category; }

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
