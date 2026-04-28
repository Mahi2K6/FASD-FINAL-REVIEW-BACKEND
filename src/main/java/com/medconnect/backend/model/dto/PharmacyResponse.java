package com.medconnect.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class PharmacyResponse {

    private Long id;
    private String storeName;
    private String pharmacistName;
    private String deliveryTime;
    private Double rating;
    private BigDecimal estimatedCost;

    public PharmacyResponse() {}

    public PharmacyResponse(Long id, String storeName, String pharmacistName, String deliveryTime, Double rating, BigDecimal estimatedCost) {
        this.id = id;
        this.storeName = storeName;
        this.pharmacistName = pharmacistName;
        this.deliveryTime = deliveryTime;
        this.rating = rating;
        this.estimatedCost = estimatedCost;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** Alias: frontend may read pharmacistId */
    @JsonProperty("pharmacistId")
    public Long getPharmacistId() { return id; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    /** Alias: frontend may read pharmacyName */
    @JsonProperty("pharmacyName")
    public String getPharmacyName() { return storeName; }

    public String getPharmacistName() { return pharmacistName; }
    public void setPharmacistName(String pharmacistName) { this.pharmacistName = pharmacistName; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    /** Alias: frontend may read estimatedDelivery */
    @JsonProperty("estimatedDelivery")
    public String getEstimatedDelivery() { return deliveryTime; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    /** Alias: frontend may read totalEstimatedPrice */
    @JsonProperty("totalEstimatedPrice")
    public BigDecimal getTotalEstimatedPrice() { return estimatedCost; }
}

