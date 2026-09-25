package com.paymesh.common.dto.biller;

public class BillerCategoryDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private boolean active;

    public BillerCategoryDto() {}

    public BillerCategoryDto(Long id, String code, String name, String description, String iconUrl, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
