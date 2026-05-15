package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ingredient_batches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;
    
    @Column(nullable = false)
    private Double quantity;
    
    @Column(nullable = false, length = 20)
    private String unit;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.ACTIVE;
    
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt = LocalDate.now();
    
    public enum BatchStatus {
        ACTIVE, EXPIRED, DEPLETED
    }
    
    // Calculate days until expiry
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    // Check if expiring soon (within 7 days)
    public boolean isExpiringSoon() {
        Long days = getDaysUntilExpiry();
        return days != null && days >= 0 && days <= 7;
    }
    
    // Check if expired
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return LocalDate.now().isAfter(expiryDate);
    }
    
    // Get risk level
    public String getExpiryRiskLevel() {
        Long days = getDaysUntilExpiry();
        if (days == null) return "NONE";
        if (days < 0) return "EXPIRED";
        if (days <= 2) return "CRITICAL";
        if (days <= 7) return "HIGH";
        if (days <= 14) return "MEDIUM";
        return "LOW";
    }
}
