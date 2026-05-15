package com.smartrestaurant.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBatchRequest {
    
    @NotNull
    private Long ingredientId;
    
    private String batchNumber; // Auto-generated if null
    
    @NotNull
    private Double quantity;
    
    @NotNull
    private LocalDate purchaseDate;
    
    private LocalDate expiryDate;
    
    private Long supplierId;
    
    private BigDecimal costPerUnit;
}
