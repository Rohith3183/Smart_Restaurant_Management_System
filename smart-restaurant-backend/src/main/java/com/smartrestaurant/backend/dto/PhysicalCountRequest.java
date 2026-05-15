package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhysicalCountRequest {
    
    @NotNull
    private Long ingredientId;
    
    @NotNull
    private Double physicalStock;
    
    private String notes;
    
    @NotNull
    private String checkedBy;
}
