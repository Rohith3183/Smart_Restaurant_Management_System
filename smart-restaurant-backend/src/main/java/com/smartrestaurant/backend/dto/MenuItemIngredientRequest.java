package com.smartrestaurant.backend.dto;

public class MenuItemIngredientRequest {
    private Long ingredientId;
    private Double quantityPerItem;

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public Double getQuantityPerItem() {
        return quantityPerItem;
    }

    public void setQuantityPerItem(Double quantityPerItem) {
        this.quantityPerItem = quantityPerItem;
    }
}
