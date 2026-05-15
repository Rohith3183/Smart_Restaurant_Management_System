package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//IngredientUsageHistoryRepository
public interface IngredientUsageHistoryRepository extends JpaRepository<IngredientUsageHistory, Long> {
 
 @Query("SELECT iuh FROM IngredientUsageHistory iuh WHERE iuh.ingredient = :ingredient " +
        "AND iuh.usageDate >= :startDate " +
        "ORDER BY iuh.usageDate DESC")
 List<IngredientUsageHistory> findRecentUsage(
     @Param("ingredient") Ingredient ingredient,
     @Param("startDate") LocalDate startDate
 );
 
 @Query("SELECT AVG(iuh.totalQuantityUsed) FROM IngredientUsageHistory iuh " +
        "WHERE iuh.ingredient = :ingredient " +
        "AND iuh.dayOfWeek = :dayOfWeek " +
        "AND iuh.usageDate >= :startDate")
 Double getAverageUsageByDayOfWeek(
     @Param("ingredient") Ingredient ingredient,
     @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
     @Param("startDate") LocalDate startDate
 );
}