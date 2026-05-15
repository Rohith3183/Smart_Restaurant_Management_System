package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.ForecastingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ForecastingServiceImpl implements ForecastingService {

    private final RestaurantRepository restaurantRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientUsageHistoryRepository usageHistoryRepository;

    public ForecastingServiceImpl(RestaurantRepository restaurantRepository,
                                  IngredientRepository ingredientRepository,
                                  IngredientUsageHistoryRepository usageHistoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.ingredientRepository = ingredientRepository;
        this.usageHistoryRepository = usageHistoryRepository;
    }

    @Override
    public List<PrepRecommendationDto> forecastForDate(String restaurantCode, LocalDate targetDate) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        LocalDate startDate = targetDate.minusWeeks(8);
        DayOfWeek dow = targetDate.getDayOfWeek();
        boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;

        List<Ingredient> ingredients = ingredientRepository.findByRestaurant(restaurant);
        List<PrepRecommendationDto> results = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            Double baseAvg = usageHistoryRepository.getAverageUsageByDayOfWeek(
                ingredient, dow, startDate
            );
            if (baseAvg == null || baseAvg <= 0) continue;

            double multiplier = 1.0;
            if (isWeekend) multiplier += 0.20; // +20% weekend
            // You can later add festival multipliers via an external calendar

            double forecast = baseAvg * multiplier;

            PrepRecommendationDto dto = new PrepRecommendationDto();
            dto.setIngredientId(ingredient.getId());
            dto.setIngredientName(ingredient.getName());
            dto.setRecommendedQuantity(forecast);
            dto.setUnit(ingredient.getUnit());
            dto.setConfidenceLevel("MEDIUM");

            results.add(dto);
        }

        return results;
    }
}
