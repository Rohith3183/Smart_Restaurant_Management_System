package com.smartrestaurant.backend.dto;

import lombok.Data;

@Data
public class PrepRecommendationDto {
    private Long ingredientId;
    private String ingredientName;
    private Double recommendedQuantity;
    private String unit;
    private String confidenceLevel; // LOW / MEDIUM / HIGH
}
