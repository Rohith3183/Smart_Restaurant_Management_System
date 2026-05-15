package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.IngredientDto;
import com.smartrestaurant.backend.entity.Ingredient;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.IngredientRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.IngredientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.dto.IngredientWithBatchesDto;
import com.smartrestaurant.backend.dto.IngredientBatchRowDto;
import com.smartrestaurant.backend.entity.IngredientBatch;
import com.smartrestaurant.backend.repository.IngredientBatchRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientServiceImpl implements IngredientService {

    private final RestaurantRepository restaurantRepository;
    private final IngredientRepository ingredientRepository;
    private final ActivityService activityService;
    private final IngredientBatchRepository ingredientBatchRepository;

    public IngredientServiceImpl(RestaurantRepository restaurantRepository,
                                 IngredientRepository ingredientRepository,
                                 ActivityService activityService,
                                 IngredientBatchRepository ingredientBatchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.ingredientRepository = ingredientRepository;
        this.activityService = activityService;
        this.ingredientBatchRepository = ingredientBatchRepository;
    }

    private Restaurant getRestaurant(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant code is required");
        }
        return restaurantRepository.findByCode(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with code " + code));
    }
    
    private String normalizeUnit(String unit) {
        if (unit == null) return null;
        switch (unit.trim().toLowerCase()) {
            case "kg":
            case "kgs":
            case "kilogram":
            case "kilograms":
            case "Kg":
                return "Kg";
            case "l":
            case "ltr":
            case "litre":
            case "litres":
            case "liter":
            case "liters":
            case "Litres":
                return "Litres";
            case "unit":
            case "units":
            case "pcs":
            case "pieces":
            case "Packets":
                return "Units";
            default:
                return unit.trim();
        }
    }

    @Override
    public List<IngredientDto> getAllIngredients(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return ingredientRepository.findByRestaurant(restaurant).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IngredientDto createIngredient(String restaurantCode, IngredientDto dto) {
    	
        System.out.println("DEBUG: createIngredient restaurantCode = " + restaurantCode);

        Restaurant restaurant = getRestaurant(restaurantCode);
        System.out.println("DEBUG: found restaurant id=" + restaurant.getId() + ", code=" + restaurant.getCode());

        Ingredient ingredient = Ingredient.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .currentStock(dto.getCurrentStock())
                .threshold(dto.getThreshold())
                .unit(normalizeUnit(dto.getUnit()))
                .expiryDate(dto.getExpiryDate())
                .build();

        System.out.println("DEBUG: ingredient before save -> restaurant=" 
                + (ingredient.getRestaurant() != null ? ingredient.getRestaurant().getId() : null));

        Ingredient saved = ingredientRepository.save(ingredient);

        System.out.println("DEBUG: ingredient saved id=" + saved.getId());

        activityService.log(
                restaurant.getCode(),
                Activity.Type.INGREDIENT_CREATED,
                String.format(
                        "Ingredient '%s' added with %.1f %s threshold %.1f %s",
                        saved.getName(),
                        saved.getCurrentStock(), saved.getUnit(),
                        saved.getThreshold(), saved.getUnit()
                ),
                "INVENTORY"
        );

        return toDto(saved);
    }


    @Override
    public IngredientDto updateIngredient(String restaurantCode, Long id, IngredientDto dto) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        if (!ingredient.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Ingredient does not belong to this restaurant");
        }

        double oldStock = ingredient.getCurrentStock();

        ingredient.setName(dto.getName());
        ingredient.setCurrentStock(dto.getCurrentStock());
        ingredient.setThreshold(dto.getThreshold());
        ingredient.setUnit(normalizeUnit(dto.getUnit()));
        ingredient.setExpiryDate(dto.getExpiryDate());

        Ingredient updated = ingredientRepository.save(ingredient);

        activityService.log(
                restaurant.getCode(),
                Activity.Type.STOCK_UPDATED,
                String.format("Stock updated: %s %.1f → %.1f %s",
                        ingredient.getName(),
                        oldStock,
                        ingredient.getCurrentStock(),
                        ingredient.getUnit()),
                "INVENTORY"
        );

        return toDto(updated);
    }

    @Override
    public void deleteIngredient(String restaurantCode, Long id) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        if (!ingredient.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Ingredient does not belong to this restaurant");
        }

        activityService.log(
                restaurant.getCode(),
                Activity.Type.INGREDIENT_DELETED,
                String.format("Ingredient '%s' removed from inventory", ingredient.getName()),
                "INVENTORY"
        );

        ingredientRepository.delete(ingredient);
    }
    
    @Override
    public java.util.List<IngredientWithBatchesDto> getIngredientsWithBatches(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);

        java.util.List<Ingredient> ingredients = ingredientRepository.findByRestaurant(restaurant);

        return ingredients.stream().map(ingredient -> {
            IngredientWithBatchesDto dto = new IngredientWithBatchesDto();
            dto.setIngredientId(ingredient.getId());
            dto.setName(ingredient.getName());

            java.util.List<IngredientBatch> batches =
                    ingredientBatchRepository.findByIngredient(ingredient);

            java.util.List<IngredientBatchRowDto> rows = batches.stream()
                    .map(b -> {
                        IngredientBatchRowDto row = new IngredientBatchRowDto();
                        row.setBatchId(b.getId());
                        row.setQuantity(b.getQuantity());
                        row.setThreshold(ingredient.getThreshold());
                        row.setUnit(b.getUnit());
                        row.setExpiryDate(b.getExpiryDate());

                        // status: EXPIRED / LOW_STOCK / OK
                        String status;
                        if (b.isExpired()) {
                            status = "EXPIRED";
                        } else if (b.getQuantity() != null
                                && ingredient.getThreshold() != null
                                && b.getQuantity() < ingredient.getThreshold()) {
                            status = "LOW_STOCK";
                        } else {
                            status = "OK";
                        }
                        row.setStatus(status);

                        return row;
                    })
                    // sort by expiry date (earliest first)
                    .sorted((a, b) -> {
                        if (a.getExpiryDate() == null && b.getExpiryDate() == null) return 0;
                        if (a.getExpiryDate() == null) return 1;
                        if (b.getExpiryDate() == null) return -1;
                        return a.getExpiryDate().compareTo(b.getExpiryDate());
                    })
                    .collect(java.util.stream.Collectors.toList());

            dto.setRows(rows);
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    private IngredientDto toDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setCurrentStock(ingredient.getCurrentStock());
        dto.setTheoreticalStock(ingredient.getTheoreticalStock());
        dto.setThreshold(ingredient.getThreshold());
        dto.setUnit(ingredient.getUnit());
        dto.setLowStock(ingredient.getCurrentStock() < ingredient.getThreshold());
        dto.setExpiryDate(ingredient.getExpiryDate());
        boolean expired = false;
        if (ingredient.getExpiryDate() != null) {
            expired = java.time.LocalDate.now().isAfter(ingredient.getExpiryDate());
        }
        dto.setExpired(expired);
        return dto;
    }
}
