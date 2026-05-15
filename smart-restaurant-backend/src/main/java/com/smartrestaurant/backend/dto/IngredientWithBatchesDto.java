package com.smartrestaurant.backend.dto;

import java.util.List;

public class IngredientWithBatchesDto {
    private Long ingredientId;
    private String name;
    private List<IngredientBatchRowDto> rows;

    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<IngredientBatchRowDto> getRows() { return rows; }
    public void setRows(List<IngredientBatchRowDto> rows) { this.rows = rows; }
}
