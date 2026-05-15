package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.BatchDto;
import com.smartrestaurant.backend.dto.CreateBatchRequest;
import java.util.List;

public interface BatchService {
    
    /**
     * Create new ingredient batch
     */
    BatchDto createBatch(String restaurantCode, CreateBatchRequest request);
    
    /**
     * Get all batches for an ingredient
     */
    List<BatchDto> getBatchesByIngredient(Long ingredientId);
    
    /**
     * Get expiring batches (within days threshold)
     */
    List<BatchDto> getExpiringBatches(String restaurantCode, Integer daysThreshold);
    
    /**
     * Mark batch as expired
     */
    void markBatchExpired(Long batchId);
    
    /**
     * Get batch details
     */
    BatchDto getBatchById(Long batchId);
}
