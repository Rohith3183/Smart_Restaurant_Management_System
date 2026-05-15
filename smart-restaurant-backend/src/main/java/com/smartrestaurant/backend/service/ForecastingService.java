package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;

import java.time.LocalDate;
import java.util.List;

public interface ForecastingService {
    List<PrepRecommendationDto> forecastForDate(String restaurantCode, LocalDate targetDate);
}
