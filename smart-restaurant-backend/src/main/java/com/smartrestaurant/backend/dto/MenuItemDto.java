package com.smartrestaurant.backend.dto;

import java.math.BigDecimal;

public class MenuItemDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private boolean available;
    
 // under existing fields
    private java.util.List<MenuItemIngredientView> ingredients;

    // nested static class
    public static class MenuItemIngredientView {
        private Long ingredientId;
        private String ingredientName;
        private Double quantityPerItem;

        public Long getIngredientId() { return ingredientId; }
        public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }

        public String getIngredientName() { return ingredientName; }
        public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

        public Double getQuantityPerItem() { return quantityPerItem; }
        public void setQuantityPerItem(Double quantityPerItem) { this.quantityPerItem = quantityPerItem; }
    }

    // Constructors
    public MenuItemDto() {}

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    
    public java.util.List<MenuItemIngredientView> getIngredients() {
        return ingredients;
    }

    public void setIngredients(java.util.List<MenuItemIngredientView> ingredients) {
        this.ingredients = ingredients;
    }
}
