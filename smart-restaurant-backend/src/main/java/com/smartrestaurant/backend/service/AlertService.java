package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AlertDto;
import com.smartrestaurant.backend.dto.CreateAlertRequest;

import java.util.List;

public interface AlertService {
    AlertDto createAlert(String restaurantCode, CreateAlertRequest request);
    List<AlertDto> getUnreadAlerts(String restaurantCode);
    void markAsRead(String restaurantCode, Long alertId);
    void checkLowStock(String restaurantCode);  // Auto-check low stock
}
