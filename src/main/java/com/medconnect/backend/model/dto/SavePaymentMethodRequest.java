package com.medconnect.backend.model.dto;

public class SavePaymentMethodRequest {
    private String cardLast4;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String tokenReference;
    private Boolean isDefault;

    // Getters and Setters
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }

    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }

    public String getTokenReference() { return tokenReference; }
    public void setTokenReference(String tokenReference) { this.tokenReference = tokenReference; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
