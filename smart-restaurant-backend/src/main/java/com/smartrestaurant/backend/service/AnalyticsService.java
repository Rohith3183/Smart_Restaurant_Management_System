package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AnalyticsDto;

public interface AnalyticsService {
    AnalyticsDto getAnalytics(String restaurantCode);
}
