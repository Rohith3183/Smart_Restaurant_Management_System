package com.smartrestaurant.backend.service;

public interface InventoryConsumptionService {

    /**
     * Deduct ingredient stock for a newly created order.
     * @param restaurantCode restaurant code (for safety)
     * @param orderId ID of the created order
     */
    void consumeIngredientsForOrder(String restaurantCode, Long orderId);
}
