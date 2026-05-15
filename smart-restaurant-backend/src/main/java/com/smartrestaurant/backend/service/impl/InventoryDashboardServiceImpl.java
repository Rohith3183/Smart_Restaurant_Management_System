package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.InventoryMetricsDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.InventoryDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryDashboardServiceImpl implements InventoryDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final ActivityRepository activityRepository;
    private final IngredientBatchRepository ingredientBatchRepository;

    public InventoryDashboardServiceImpl(RestaurantRepository restaurantRepository,
                                         IngredientRepository ingredientRepository,
                                         MenuItemRepository menuItemRepository,
                                         ActivityRepository activityRepository,
                                         IngredientBatchRepository ingredientBatchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemRepository = menuItemRepository;
        this.activityRepository = activityRepository;
        this.ingredientBatchRepository = ingredientBatchRepository;
    }

    private Restaurant getRestaurant(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Override
    public InventoryMetricsDto getMetrics(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        InventoryMetricsDto dto = new InventoryMetricsDto();

        computeWastagePercent(restaurant, dto);
        computeStockoutsThisWeek(restaurant, dto);
        computeTheftRiskLevel(restaurant, dto);
        computeExpiryRiskLevel(restaurant, dto);

        return dto;
    }

    // --- Wastage %: (total variance loss / theoretical stock) * 100 ---
    private void computeWastagePercent(Restaurant restaurant, InventoryMetricsDto dto) {
        List<Ingredient> ingredients = ingredientRepository.findByRestaurant(restaurant);

        double totalTheoretical = 0.0;
        double totalVarianceLoss = 0.0; // only negative variance

        for (Ingredient ing : ingredients) {
            if (!Boolean.TRUE.equals(ing.getIsTrackedForVariance())) continue;

            Double theoretical = ing.getTheoreticalStock();
            Double current = ing.getCurrentStock();
            if (theoretical == null || theoretical <= 0 || current == null) continue;

            double variance = current - theoretical;
            totalTheoretical += theoretical;

            if (variance < 0) {
                totalVarianceLoss += (-variance);
            }
        }

        if (totalTheoretical <= 0) {
            dto.setWastagePercent(null);
            return;
        }

        double pct = (totalVarianceLoss / totalTheoretical) * 100.0;
        dto.setWastagePercent(pct);
    }

    // --- Stockouts this week: count MENU_UPDATED "marked UNAVAILABLE" activities ---
    private void computeStockoutsThisWeek(Restaurant restaurant, InventoryMetricsDto dto) {
        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(23, 59, 59);

        List<Activity> activities = activityRepository.findByRestaurantAndTypeAndCreatedAtBetween(
                restaurant,
                Activity.Type.MENU_UPDATED,
                startOfWeek,
                endOfToday
        );

        int stockouts = 0;
        for (Activity act : activities) {
            String msg = act.getMessage();
            if (msg != null && msg.contains("marked UNAVAILABLE")) {
                stockouts++;
            }
        }

        dto.setStockoutsThisWeek(stockouts);
    }

    // --- Theft risk: based on variance percentage across tracked ingredients ---
    private void computeTheftRiskLevel(Restaurant restaurant, InventoryMetricsDto dto) {
        List<Ingredient> ingredients = ingredientRepository.findByRestaurant(restaurant);

        int highRiskCount = 0;
        int mediumRiskCount = 0;

        for (Ingredient ing : ingredients) {
            if (!Boolean.TRUE.equals(ing.getIsTrackedForVariance())) continue;

            Double theoretical = ing.getTheoreticalStock();
            Double current = ing.getCurrentStock();
            if (theoretical == null || theoretical <= 0 || current == null) continue;

            double variance = current - theoretical;
            double variancePct = (variance / theoretical) * 100.0;

            if (variancePct < -20.0) {
                highRiskCount++;
            } else if (variancePct < -10.0) {
                mediumRiskCount++;
            }
        }

        String level;
        if (highRiskCount >= 3) {
            level = "HIGH";
        } else if (highRiskCount >= 1 || mediumRiskCount >= 3) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        dto.setTheftRiskLevel(level);
    }

    // --- Expiry risk: based on batches expiring soon vs total ---
    private void computeExpiryRiskLevel(Restaurant restaurant, InventoryMetricsDto dto) {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        // all active batches for restaurant
        List<IngredientBatch> allBatches = ingredientBatchRepository.findByIngredientRestaurant(restaurant);

        int totalActive = 0;
        int expiringSoon = 0;
        int expired = 0;

        for (IngredientBatch b : allBatches) {
            if (b.getStatus() != IngredientBatch.BatchStatus.ACTIVE) continue;

            totalActive++;
            if (b.getExpiryDate() == null) continue;

            if (today.isAfter(b.getExpiryDate())) {
                expired++;
            } else if (!b.getExpiryDate().isAfter(soon)) {
                expiringSoon++;
            }
        }

        if (totalActive == 0) {
            dto.setExpiryRiskLevel("LOW");
            return;
        }

        double ratioExpiring = (double) expiringSoon / totalActive;
        double ratioExpired = (double) expired / totalActive;

        String level;
        if (ratioExpired > 0.2 || ratioExpiring > 0.4) {
            level = "HIGH";
        } else if (ratioExpired > 0.05 || ratioExpiring > 0.2) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        dto.setExpiryRiskLevel(level);
    }
}
