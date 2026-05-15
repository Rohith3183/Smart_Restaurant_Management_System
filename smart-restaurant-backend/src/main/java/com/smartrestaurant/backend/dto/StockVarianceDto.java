package com.smartrestaurant.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StockVarianceDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private LocalDate checkDate;
    private Double theoreticalStock;
    private Double physicalStock;
    private Double variance;
    private Double variancePercentage;
    private String riskLevel;
    private String notes;
    private String checkedBy;
}
