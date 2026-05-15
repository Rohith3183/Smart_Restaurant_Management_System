package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.IngredientDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.StockDeductionService;
import com.smartrestaurant.backend.service.StockUpdateNotifier;
import com.smartrestaurant.backend.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class StockDeductionServiceImpl implements StockDeductionService {
    
    private final IngredientRepository ingredientRepository;
    private final IngredientBatchRepository batchRepository;
    private final StockDeductionRepository deductionRepository;
    private final MenuItemIngredientRepository menuItemIngredientRepository;
    private final RestaurantRepository restaurantRepository;
    private final ActivityService activityService;
    private final StockUpdateNotifier stockUpdateNotifier;
    
    public StockDeductionServiceImpl(
            IngredientRepository ingredientRepository,
            IngredientBatchRepository batchRepository,
            StockDeductionRepository deductionRepository,
            MenuItemIngredientRepository menuItemIngredientRepository,
            RestaurantRepository restaurantRepository,
            ActivityService activityService,
            StockUpdateNotifier stockUpdateNotifier) {
        this.ingredientRepository = ingredientRepository;
        this.batchRepository = batchRepository;
        this.deductionRepository = deductionRepository;
        this.menuItemIngredientRepository = menuItemIngredientRepository;
        this.restaurantRepository = restaurantRepository;
        this.activityService = activityService;
        this.stockUpdateNotifier = stockUpdateNotifier;
    }
    
    @Override
    public List<StockDeduction> processOrderDeduction(OrderEntity order) {
        log.info("Processing stock deduction for order ID: {}", order.getId());
        
        List<StockDeduction> deductions = new ArrayList<>();
        
        // Calculate total requirements
        Map<Long, Double> requirements = calculateOrderRequirements(order);
        
        // Process each ingredient
        for (Map.Entry<Long, Double> entry : requirements.entrySet()) {
            Long ingredientId = entry.getKey();
            Double quantityNeeded = entry.getValue();
            
            Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + ingredientId));
            
            // Deduct using FEFO logic
            List<StockDeduction> ingredientDeductions = deductFromBatches(
                ingredient, quantityNeeded, order
            );
            
            deductions.addAll(ingredientDeductions);
            
            // Update theoretical stock
            Double stockBefore = ingredient.getTheoreticalStock();
            ingredient.setTheoreticalStock(stockBefore - quantityNeeded);
//            ingredientRepository.save(ingredient);
            Ingredient savedIngredient = ingredientRepository.save(ingredient);
            
         // ✅ BROADCAST REAL-TIME UPDATE
            IngredientDto dto = convertToDto(savedIngredient);
            stockUpdateNotifier.notifyStockUpdate(
                order.getRestaurant().getCode(), 
                dto
            );
            
            // Log activity
            activityService.log(
                order.getRestaurant().getCode(),
                Activity.Type.STOCK_UPDATED,
                String.format("Auto-deducted %.2f %s of %s for Order #%d",
                    quantityNeeded, ingredient.getUnit(), ingredient.getName(), order.getId()),
                "SYSTEM"
            );
            
            // Check low stock
            if (ingredient.getTheoreticalStock() <= ingredient.getThreshold()) {
                log.warn("LOW STOCK ALERT: {} is below threshold!", ingredient.getName());
            }
        }
        
        log.info("Deduction complete. Total deductions: {}", deductions.size());
        return deductions;
    }
    
    private IngredientDto convertToDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setCurrentStock(ingredient.getCurrentStock());
        dto.setThreshold(ingredient.getThreshold());
        dto.setUnit(ingredient.getUnit());
        dto.setLowStock(ingredient.isLowStock());
        return dto;
    }
    
    /**
     * FEFO Logic: First Expire First Out
     */
    private List<StockDeduction> deductFromBatches(
            Ingredient ingredient, Double quantityNeeded, OrderEntity order) {
        
        List<StockDeduction> deductions = new ArrayList<>();
        
        // Get active batches sorted by FEFO (expiry date, then purchase date)
        List<IngredientBatch> batches = batchRepository.findActiveBatchesByFEFO(ingredient);
        
        Double remainingQuantity = quantityNeeded;
        
        for (IngredientBatch batch : batches) {
            if (remainingQuantity <= 0) break;
            
            if (batch.getQuantity() > 0) {
                Double deductFromBatch = Math.min(batch.getQuantity(), remainingQuantity);
                
                // Create deduction record
                StockDeduction deduction = StockDeduction.builder()
                    .ingredient(ingredient)
                    .batch(batch)
                    .order(order)
                    .quantityDeducted(deductFromBatch)
                    .theoreticalStockBefore(ingredient.getTheoreticalStock())
                    .theoreticalStockAfter(ingredient.getTheoreticalStock() - deductFromBatch)
                    .deductionType(StockDeduction.DeductionType.ORDER)
                    .deductedAt(LocalDateTime.now())
                    .deductedBy("SYSTEM")
                    .notes(String.format("Order #%d - Batch %s", 
                        order.getId(), batch.getBatchNumber()))
                    .build();
                
                deductions.add(deductionRepository.save(deduction));
                
                // Update batch quantity
                batch.setQuantity(batch.getQuantity() - deductFromBatch);
                if (batch.getQuantity() <= 0) {
                    batch.setStatus(IngredientBatch.BatchStatus.DEPLETED);
                }
                batchRepository.save(batch);
                
                remainingQuantity -= deductFromBatch;
                
                log.info("Deducted {} {} from batch {} (remaining in batch: {})",
                    deductFromBatch, ingredient.getUnit(), 
                    batch.getBatchNumber(), batch.getQuantity());
            }
        }
        
        // Warning if couldn't fulfill complete requirement
        if (remainingQuantity > 0) {
            log.warn("INSUFFICIENT STOCK: Could not deduct full quantity for {}. Short by: {} {}",
                ingredient.getName(), remainingQuantity, ingredient.getUnit());
        }
        
        return deductions;
    }
    
    @Override
    public Map<Long, Double> calculateOrderRequirements(OrderEntity order) {
        Map<Long, Double> requirements = new HashMap<>();
        
        for (OrderItem orderItem : order.getItems()) {
            MenuItem menuItem = orderItem.getMenuItem();
            Integer quantity = orderItem.getQuantity();
            
            // Get recipe (ingredients for this menu item)
            List<MenuItemIngredient> recipe = menuItem.getIngredients();
            
            if (recipe == null || recipe.isEmpty()) {
                log.warn("No recipe found for menu item: {}", menuItem.getName());
                continue;
            }
            
            // Calculate ingredient requirements
            for (MenuItemIngredient recipeItem : recipe) {
                Long ingredientId = recipeItem.getIngredient().getId();
                Double quantityPerItem = recipeItem.getQuantityPerItem();
                Double totalNeeded = quantityPerItem * quantity;
                
                // Accumulate if same ingredient appears multiple times
                requirements.merge(ingredientId, totalNeeded, Double::sum);
            }
        }
        
        return requirements;
    }
    
    @Override
    public StockDeduction manualAdjustment(
            String restaurantCode, Long ingredientId, Double quantity,
            String deductionType, String notes, String deductedBy) {
        
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        if (!ingredient.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Ingredient does not belong to this restaurant");
        }
        
        Double stockBefore = ingredient.getTheoreticalStock();
        Double stockAfter = stockBefore - quantity;
        
        StockDeduction deduction = StockDeduction.builder()
            .ingredient(ingredient)
            .quantityDeducted(quantity)
            .theoreticalStockBefore(stockBefore)
            .theoreticalStockAfter(stockAfter)
            .deductionType(StockDeduction.DeductionType.valueOf(deductionType))
            .deductedAt(LocalDateTime.now())
            .deductedBy(deductedBy)
            .notes(notes)
            .build();
        
        StockDeduction saved = deductionRepository.save(deduction);
        
        // Update theoretical stock
        ingredient.setTheoreticalStock(stockAfter);
        ingredientRepository.save(ingredient);
        
        // Log activity
        activityService.log(
            restaurantCode,
            Activity.Type.STOCK_UPDATED,
            String.format("Manual adjustment: %s %.2f %s of %s",
                quantity > 0 ? "Deducted" : "Added",
                Math.abs(quantity), ingredient.getUnit(), ingredient.getName()),
            deductedBy
        );
        
        return saved;
    }
    
    @Override
    public List<StockDeduction> getDeductionHistory(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        return deductionRepository.findByIngredientOrderByDeductedAtDesc(ingredient);
    }
}
