package com.medconnect.backend.model.dto;

/**
 * Partial update for inventory: only non-null fields are applied.
 */
public class InventoryUpdateRequest {

    private Integer quantity;
    private Double price;
    private Integer minThreshold;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(Integer minThreshold) {
        this.minThreshold = minThreshold;
    }
}
