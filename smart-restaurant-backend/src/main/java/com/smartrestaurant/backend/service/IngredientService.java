package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.IngredientDto;
import com.smartrestaurant.backend.dto.IngredientWithBatchesDto;

import java.util.List;

public interface IngredientService {
    List<IngredientDto> getAllIngredients(String restaurantCode);
    IngredientDto createIngredient(String restaurantCode, IngredientDto dto);
    IngredientDto updateIngredient(String restaurantCode, Long id, IngredientDto dto);
    void deleteIngredient(String restaurantCode, Long id);
    
    List<IngredientWithBatchesDto> getIngredientsWithBatches(String restaurantCode);
}
