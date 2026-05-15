package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_deductions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeduction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private IngredientBatch batch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    
    @Column(name = "quantity_deducted", nullable = false)
    private Double quantityDeducted;
    
    @Column(name = "theoretical_stock_before", nullable = false)
    private Double theoreticalStockBefore;
    
    @Column(name = "theoretical_stock_after", nullable = false)
    private Double theoreticalStockAfter;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deduction_type", nullable = false)
    private DeductionType deductionType;
    
    @Column(name = "deducted_at", updatable = false)
    private LocalDateTime deductedAt = LocalDateTime.now();
    
    @Column(name = "deducted_by")
    private String deductedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    public enum DeductionType {
        ORDER, MANUAL, WASTAGE, PREP, ADJUSTMENT
    }
}
