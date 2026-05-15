package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "current_stock", nullable = false)
    private Double currentStock;
    
    // NEW: Theoretical stock (calculated from orders)
    @Column(name = "theoretical_stock", nullable = false)
    private Double theoreticalStock = 0.0;
    
    @Column(nullable = false)
    private Double threshold;
    
    private String unit;
    private LocalDate expiryDate;
    
    // NEW: Variance tracking
    @Column(name = "last_physical_count_date")
    private LocalDate lastPhysicalCountDate;
    
    @Column(name = "is_tracked_for_variance")
    private Boolean isTrackedForVariance = false;
    
    // Helper method to check low stock
    public boolean isLowStock() {
        return currentStock <= threshold;
    }
    
    // Helper method to calculate variance
    public Double getStockVariance() {
        if (theoreticalStock == null || currentStock == null) return 0.0;
        return currentStock - theoreticalStock;
    }
    
    // Helper method to calculate variance percentage
    public Double getVariancePercentage() {
        if (theoreticalStock == null || theoreticalStock == 0) return 0.0;
        return (getStockVariance() / theoreticalStock) * 100;
    }
}
