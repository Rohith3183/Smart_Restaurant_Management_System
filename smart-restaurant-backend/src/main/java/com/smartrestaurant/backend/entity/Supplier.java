package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "on_time_deliveries")
    private Integer onTimeDeliveries = 0;
    
    @Column(name = "quality_issues")
    private Integer qualityIssues = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Calculate on-time delivery percentage
    public Double getOnTimePercentage() {
        if (totalOrders == null || totalOrders == 0) return 0.0;
        return (onTimeDeliveries.doubleValue() / totalOrders.doubleValue()) * 100;
    }
    
    // Calculate quality score - FIXED
    public Double getQualityScore() {
        if (totalOrders == null || totalOrders == 0) return 100.0;
        // Cast to double directly instead of using .doubleValue()
        return ((double)(totalOrders - qualityIssues) / totalOrders) * 100;
    }
}
