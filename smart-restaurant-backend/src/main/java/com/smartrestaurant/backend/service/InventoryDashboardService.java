package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.InventoryMetricsDto;

public interface InventoryDashboardService {

    InventoryMetricsDto getMetrics(String restaurantCode);
}
