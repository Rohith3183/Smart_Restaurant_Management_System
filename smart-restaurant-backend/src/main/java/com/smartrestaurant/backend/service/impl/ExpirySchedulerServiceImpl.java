package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.IngredientDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.IngredientBatchRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.service.AlertService;
import com.smartrestaurant.backend.service.ExpirySchedulerService;
import com.smartrestaurant.backend.service.StockUpdateNotifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpirySchedulerServiceImpl implements ExpirySchedulerService {

    private final RestaurantRepository restaurantRepository;
    private final IngredientBatchRepository batchRepository;
    private final ActivityService activityService;
    private final AlertService alertService;
    private final StockUpdateNotifier stockUpdateNotifier;

    public ExpirySchedulerServiceImpl(RestaurantRepository restaurantRepository,
                                      IngredientBatchRepository batchRepository,
                                      ActivityService activityService,
                                      AlertService alertService,
                                      StockUpdateNotifier stockUpdateNotifier) {
        this.restaurantRepository = restaurantRepository;
        this.batchRepository = batchRepository;
        this.activityService = activityService;
        this.alertService = alertService;
        this.stockUpdateNotifier = stockUpdateNotifier;
    }

    // Run every day at 05:00
    @Override
    @Scheduled(cron = "0 0 5 * * *")
    public void scanAndHandleExpiringBatches() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant restaurant : restaurants) {
            List<IngredientBatch> expiring = batchRepository.findExpiringBatches(restaurant, today, soon);

            if (expiring.isEmpty()) continue;

            String restaurantCode = restaurant.getCode();

            for (IngredientBatch batch : expiring) {
                Ingredient ingredient = batch.getIngredient();
                Long days = batch.getDaysUntilExpiry();

                // Create alert for owner
                String msg = String.format(
                    "Batch %s of %s (%.2f %s) will expire in %d day(s).",
                    batch.getBatchNumber(),
                    ingredient.getName(),
                    batch.getQuantity(),
                    ingredient.getUnit(),
                    days != null ? days : -1
                );

                com.smartrestaurant.backend.dto.CreateAlertRequest alertReq =
                        new com.smartrestaurant.backend.dto.CreateAlertRequest();
                alertReq.setMessage(msg);
                alertReq.setFromRole("SYSTEM");        // adjust field names if different
                // If your DTO has category/type, set it here; otherwise remove this line
                // alertReq.setCategory("EXPIRY");

                alertService.createAlert(restaurantCode, alertReq);

                activityService.log(
                    restaurantCode,
                    Activity.Type.STOCK_UPDATED,
                    "Expiry warning: " + msg,
                    "SYSTEM"
                );
            }

            // Optionally push a lightweight expiry summary via WebSocket
            List<IngredientDto> affectedIngredients = expiring.stream()
                .map(IngredientBatch::getIngredient)
                .distinct()
                .map(this::toIngredientDto)
                .collect(Collectors.toList());

            if (!affectedIngredients.isEmpty()) {
                stockUpdateNotifier.notifyExpiryWarning(
                    restaurantCode,
                    String.format("%d ingredients have batches expiring within 7 days.", affectedIngredients.size())
                );
            }
        }
    }

    private IngredientDto toIngredientDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setCurrentStock(ingredient.getCurrentStock());
        dto.setThreshold(ingredient.getThreshold());
        dto.setUnit(ingredient.getUnit());
        dto.setLowStock(ingredient.getCurrentStock() <= ingredient.getThreshold());
        return dto;
    }
}
