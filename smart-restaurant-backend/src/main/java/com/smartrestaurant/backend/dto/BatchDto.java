package com.smartrestaurant.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchDto {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String batchNumber;
    private Double quantity;
    private String unit;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private Long supplierId;
    private String supplierName;
    private BigDecimal costPerUnit;
    private String status;
    private Long daysUntilExpiry;
    private String expiryRiskLevel;
}
