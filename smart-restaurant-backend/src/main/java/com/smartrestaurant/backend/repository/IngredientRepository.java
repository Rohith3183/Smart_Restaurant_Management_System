// IngredientRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Ingredient;
import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByRestaurant(Restaurant restaurant);
    
    List<Ingredient> findByRestaurantAndExpiryDateBetweenOrderByExpiryDateAsc(
            Restaurant restaurant,
            LocalDate startDate,
            LocalDate endDate
    );
}
