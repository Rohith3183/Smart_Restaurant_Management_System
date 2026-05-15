package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prep_recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;
    
    @Column(name = "predicted_requirement", nullable = false)
    private Double predictedRequirement;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel = ConfidenceLevel.MEDIUM;
    
    @Column(name = "based_on_weeks")
    private Integer basedOnWeeks = 4;
    
    @Column(name = "actual_usage")
    private Double actualUsage;
    
    @Column(name = "accuracy_percentage")
    private Double accuracyPercentage;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum ConfidenceLevel {
        LOW, MEDIUM, HIGH
    }
}
