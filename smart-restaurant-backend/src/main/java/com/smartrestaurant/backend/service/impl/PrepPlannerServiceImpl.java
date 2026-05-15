package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.service.PrepPlannerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrepPlannerServiceImpl implements PrepPlannerService {

    private final RestaurantRepository restaurantRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientUsageHistoryRepository usageHistoryRepository;
    private final PrepRecommendationRepository prepRecommendationRepository;
    private final ActivityService activityService;

    public PrepPlannerServiceImpl(RestaurantRepository restaurantRepository,
                                  IngredientRepository ingredientRepository,
                                  IngredientUsageHistoryRepository usageHistoryRepository,
                                  PrepRecommendationRepository prepRecommendationRepository,
                                  ActivityService activityService) {
        this.restaurantRepository = restaurantRepository;
        this.ingredientRepository = ingredientRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.prepRecommendationRepository = prepRecommendationRepository;
        this.activityService = activityService;
    }

    // Run every day at 03:00
    @Override
    @Scheduled(cron = "0 0 3 * * *")
    public void generateTodayPrepPlan() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(4);
        DayOfWeek todayDOW = today.getDayOfWeek();

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            List<Ingredient> ingredients = ingredientRepository.findByRestaurant(restaurant);

            for (Ingredient ingredient : ingredients) {
                // Average usage for this ingredient on the same day of week in last 4 weeks
                Double avgUsage = usageHistoryRepository.getAverageUsageByDayOfWeek(
                    ingredient,
                    todayDOW,
                    startDate
                );

                if (avgUsage == null || avgUsage <= 0) {
                    continue;
                }

                double recommended = avgUsage;

                PrepRecommendation.ConfidenceLevel confidence;
                if (avgUsage >= 5) {
                    confidence = PrepRecommendation.ConfidenceLevel.HIGH;
                } else if (avgUsage <= 2) {
                    confidence = PrepRecommendation.ConfidenceLevel.LOW;
                } else {
                    confidence = PrepRecommendation.ConfidenceLevel.MEDIUM;
                }

                PrepRecommendation recommendation = PrepRecommendation.builder()
                    .restaurant(restaurant)
                    .ingredient(ingredient)
                    .recommendationDate(today)
                    .predictedRequirement(recommended)
                    .confidenceLevel(confidence)
                    .basedOnWeeks(4)
                    .build();

                final PrepRecommendation.ConfidenceLevel finalConfidence = confidence;

                prepRecommendationRepository
                    .findByRestaurantAndRecommendationDate(restaurant, today).stream()
                    .filter(r -> r.getIngredient().getId().equals(ingredient.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                        existing -> {
                            existing.setPredictedRequirement(recommended);
                            existing.setConfidenceLevel(finalConfidence);
                            prepRecommendationRepository.save(existing);
                        },
                        () -> prepRecommendationRepository.save(recommendation)
                    );
            }

            activityService.log(
                restaurant.getCode(),
                Activity.Type.STOCK_UPDATED,
                "Daily prep plan generated for " + today.toString(),
                "SYSTEM"
            );
        }
    }

    @Override
    public List<PrepRecommendationDto> getTodayPrepPlan(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        LocalDate today = LocalDate.now();

        List<PrepRecommendation> recs = prepRecommendationRepository
            .findTodaysRecommendations(restaurant, today);

        List<PrepRecommendationDto> dtos = new ArrayList<>();
        for (PrepRecommendation pr : recs) {
            PrepRecommendationDto dto = new PrepRecommendationDto();
            dto.setIngredientId(pr.getIngredient().getId());
            dto.setIngredientName(pr.getIngredient().getName());
            dto.setRecommendedQuantity(pr.getPredictedRequirement());
            dto.setUnit(pr.getIngredient().getUnit());
            dto.setConfidenceLevel(pr.getConfidenceLevel().name());
            dtos.add(dto);
        }
        return dtos.stream()
                .sorted((a,b) -> Double.compare(
                    b.getRecommendedQuantity(), a.getRecommendedQuantity()))
                .collect(Collectors.toList());
    }
}
