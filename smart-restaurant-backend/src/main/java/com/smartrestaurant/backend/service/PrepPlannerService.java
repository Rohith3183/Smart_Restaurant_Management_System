package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;

import java.util.List;

public interface PrepPlannerService {
    void generateTodayPrepPlan();
    List<PrepRecommendationDto> getTodayPrepPlan(String restaurantCode);
}
