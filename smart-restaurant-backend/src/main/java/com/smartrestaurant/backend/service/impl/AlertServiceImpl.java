package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.AlertDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.dto.CreateAlertRequest;
import com.smartrestaurant.backend.entity.Alert;
import com.smartrestaurant.backend.entity.Ingredient;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.AlertRepository;
import com.smartrestaurant.backend.repository.IngredientRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final RestaurantRepository restaurantRepository;
    private final IngredientRepository ingredientRepository;
    private final ActivityService activityService;

    public AlertServiceImpl(AlertRepository alertRepository,
                           RestaurantRepository restaurantRepository,
                           IngredientRepository ingredientRepository,
                           ActivityService activityService) {
        this.alertRepository = alertRepository;
        this.restaurantRepository = restaurantRepository;
        this.ingredientRepository = ingredientRepository;
        this.activityService = activityService;
    }

    @Override
    public AlertDto createAlert(String restaurantCode, CreateAlertRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Alert alert = Alert.builder()
                .restaurant(restaurant)
                .type(Alert.Type.ISSUE)
                .message(request.getMessage())
                .fromRole(Alert.FromRole.valueOf(request.getFromRole()))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Alert saved = alertRepository.save(alert);
        return toDto(saved);
    }

    @Override
    public List<AlertDto> getUnreadAlerts(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return alertRepository.findByRestaurantAndIsReadFalse(restaurant).stream()
                .map(this::toDto)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String restaurantCode, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        if (!alert.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Alert does not belong to this restaurant");
        }

        alertRepository.delete(alert);  // Delete instead of marking as read
    }

    @Override
    public void checkLowStock(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        List<Ingredient> lowStockIngredients = ingredientRepository.findByRestaurant(restaurant).stream()
                .filter(ing -> ing.getCurrentStock() < ing.getThreshold())
                .collect(Collectors.toList());

        for (Ingredient ingredient : lowStockIngredients) {
            // Check if alert already exists
            boolean alertExists = alertRepository.findByRestaurantAndIsReadFalse(restaurant).stream()
                    .anyMatch(alert -> alert.getMessage().contains(ingredient.getName()));

            if (!alertExists) {
                Alert alert = Alert.builder()
                        .restaurant(restaurant)
                        .type(Alert.Type.STOCK)
                        .message("Low stock alert: " + ingredient.getName() + " (Current: " + ingredient.getCurrentStock() + " " + ingredient.getUnit() + ", Threshold: " + ingredient.getThreshold() + ")")
                        .fromRole(Alert.FromRole.SYSTEM)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                alertRepository.save(alert);
                
                activityService.log(
                	    restaurant.getCode(),
                	    Activity.Type.LOW_STOCK_ALERT,
                	    String.format("Low stock alert: %s current %.1f %s (threshold %.1f %s)",
                	                  ingredient.getName(),
                	                  ingredient.getCurrentStock(), ingredient.getUnit(),
                	                  ingredient.getThreshold(), ingredient.getUnit()),
                	    "SYSTEM"
                	);
            }
        }
    }

    private AlertDto toDto(Alert alert) {
        AlertDto dto = new AlertDto();
        dto.setId(alert.getId());
        dto.setType(alert.getType().name());
        dto.setMessage(alert.getMessage());
        dto.setFromRole(alert.getFromRole().name());
        dto.setRead(alert.isRead());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }
}
