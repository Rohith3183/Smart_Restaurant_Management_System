package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.entity.Ingredient;
import com.smartrestaurant.backend.entity.OrderEntity;
import com.smartrestaurant.backend.entity.StockDeduction;
import java.util.List;
import java.util.Map;

public interface StockDeductionService {
    
    /**
     * Auto-deduct stock based on order items (triggered when order is SERVED)
     * @param order The completed order
     * @return List of stock deductions performed
     */
    List<StockDeduction> processOrderDeduction(OrderEntity order);
    
    /**
     * Manual stock adjustment
     */
    StockDeduction manualAdjustment(String restaurantCode, Long ingredientId, 
                                   Double quantity, String deductionType, 
                                   String notes, String deductedBy);
    
    /**
     * Get deduction history for an ingredient
     */
    List<StockDeduction> getDeductionHistory(Long ingredientId);
    
    /**
     * Calculate total ingredient requirements for an order
     */
    Map<Long, Double> calculateOrderRequirements(OrderEntity order);
}
