package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.StockVarianceDto;
import com.smartrestaurant.backend.dto.PhysicalCountRequest;
import java.util.List;

public interface StockVarianceService {
    
    /**
     * Record physical stock count and calculate variance
     */
    StockVarianceDto recordPhysicalCount(String restaurantCode, PhysicalCountRequest request);
    
    /**
     * Get variance history for an ingredient
     */
    List<StockVarianceDto> getVarianceHistory(Long ingredientId);
    
    /**
     * Get high-risk variances for restaurant
     */
    List<StockVarianceDto> getHighRiskVariances(String restaurantCode);
    
    /**
     * Get today's variance summary
     */
    List<StockVarianceDto> getTodaysVariances(String restaurantCode);
}
