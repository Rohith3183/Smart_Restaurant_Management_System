package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.smartrestaurant.backend.dto.MenuItemIngredientRequest;
import java.math.BigDecimal;

public class CreateMenuItemRequest {
    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal price;

    private boolean available = true;
    
    private java.util.List<MenuItemIngredientRequest> majorIngredients;

    // Getters and setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public java.util.List<MenuItemIngredientRequest> getMajorIngredients() {
        return majorIngredients;
    }

    public void setMajorIngredients(java.util.List<MenuItemIngredientRequest> majorIngredients) {
        this.majorIngredients = majorIngredients;
    }

}
