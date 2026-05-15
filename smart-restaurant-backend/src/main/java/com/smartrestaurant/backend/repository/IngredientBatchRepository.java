package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

// IngredientBatchRepository
public interface IngredientBatchRepository extends JpaRepository<IngredientBatch, Long> {
    
    List<IngredientBatch> findByIngredientAndStatus(Ingredient ingredient, IngredientBatch.BatchStatus status);
    
    @Query("SELECT b FROM IngredientBatch b WHERE b.ingredient = :ingredient " +
           "AND b.status = 'ACTIVE' AND b.quantity > 0 " +
           "ORDER BY b.expiryDate ASC, b.purchaseDate ASC")
    List<IngredientBatch> findActiveBatchesByFEFO(@Param("ingredient") Ingredient ingredient);
    
    @Query("SELECT b FROM IngredientBatch b WHERE b.ingredient.restaurant = :restaurant " +
           "AND b.expiryDate BETWEEN :startDate AND :endDate " +
           "AND b.status = 'ACTIVE'")
    List<IngredientBatch> findExpiringBatches(
        @Param("restaurant") Restaurant restaurant,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

	List<IngredientBatch> findByIngredient(Ingredient ingredient);
	
	List<IngredientBatch> findByIngredientRestaurant(Restaurant restaurant);
}
