package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_variance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockVariance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;
    
    @Column(name = "theoretical_stock", nullable = false)
    private Double theoreticalStock;
    
    @Column(name = "physical_stock", nullable = false)
    private Double physicalStock;
    
    @Column(nullable = false)
    private Double variance;
    
    @Column(name = "variance_percentage", nullable = false)
    private Double variancePercentage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "checked_by")
    private String checkedBy;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // Calculate risk level based on variance percentage
    public static RiskLevel calculateRiskLevel(Double variancePercentage) {
        double absVariance = Math.abs(variancePercentage);
        if (absVariance >= 20) return RiskLevel.CRITICAL;
        if (absVariance >= 10) return RiskLevel.HIGH;
        if (absVariance >= 5) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}
