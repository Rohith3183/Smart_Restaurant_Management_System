package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//StockVarianceRepository
public interface StockVarianceRepository extends JpaRepository<StockVariance, Long> {
 
 List<StockVariance> findByIngredientOrderByCheckDateDesc(Ingredient ingredient);
 
 List<StockVariance> findByCheckDateAndIngredient_Restaurant(LocalDate checkDate, Restaurant restaurant);
 
 @Query("SELECT sv FROM StockVariance sv WHERE sv.ingredient.restaurant = :restaurant " +
        "AND sv.riskLevel IN ('HIGH', 'CRITICAL') " +
        "ORDER BY sv.checkDate DESC")
 List<StockVariance> findHighRiskVariances(@Param("restaurant") Restaurant restaurant);
}