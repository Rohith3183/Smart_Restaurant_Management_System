package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//StockDeductionRepository
public interface StockDeductionRepository extends JpaRepository<StockDeduction, Long> {
 
 List<StockDeduction> findByIngredientOrderByDeductedAtDesc(Ingredient ingredient);
 
 List<StockDeduction> findByOrder(OrderEntity order);
 
 @Query("SELECT sd FROM StockDeduction sd WHERE sd.ingredient.restaurant = :restaurant " +
        "AND sd.deductedAt >= :startDate")
 List<StockDeduction> findRecentDeductions(
     @Param("restaurant") Restaurant restaurant,
     @Param("startDate") java.time.LocalDateTime startDate
 );
}