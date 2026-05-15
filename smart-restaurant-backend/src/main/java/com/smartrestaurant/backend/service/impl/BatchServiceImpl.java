package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.BatchDto;
import com.smartrestaurant.backend.dto.CreateBatchRequest;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.BatchService;
import com.smartrestaurant.backend.service.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchServiceImpl implements BatchService {
    
    private final IngredientBatchRepository batchRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final RestaurantRepository restaurantRepository;
    private final ActivityService activityService;
    
    public BatchServiceImpl(
            IngredientBatchRepository batchRepository,
            IngredientRepository ingredientRepository,
            SupplierRepository supplierRepository,
            RestaurantRepository restaurantRepository,
            ActivityService activityService) {
        this.batchRepository = batchRepository;
        this.ingredientRepository = ingredientRepository;
        this.supplierRepository = supplierRepository;
        this.restaurantRepository = restaurantRepository;
        this.activityService = activityService;
    }
    
    @Override
    public BatchDto createBatch(String restaurantCode, CreateBatchRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        if (!ingredient.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Ingredient does not belong to this restaurant");
        }
        
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        }
        
        // Generate batch number if not provided
        String batchNumber = request.getBatchNumber();
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            batchNumber = generateBatchNumber(ingredient);
        }
        
        IngredientBatch batch = IngredientBatch.builder()
            .ingredient(ingredient)
            .batchNumber(batchNumber)
            .quantity(request.getQuantity())
            .unit(ingredient.getUnit())
            .purchaseDate(request.getPurchaseDate())
            .expiryDate(request.getExpiryDate())
            .supplier(supplier)
            .costPerUnit(request.getCostPerUnit())
            .status(IngredientBatch.BatchStatus.ACTIVE)
            .build();
        
        IngredientBatch saved = batchRepository.save(batch);
        
        // Update ingredient current stock
        ingredient.setCurrentStock(ingredient.getCurrentStock() + request.getQuantity());
        ingredientRepository.save(ingredient);
        
        // Log activity
        activityService.log(
            restaurantCode,
            Activity.Type.STOCK_UPDATED,
            String.format("Added batch %s: %.2f %s of %s",
                saved.getBatchNumber(), request.getQuantity(), 
                ingredient.getUnit(), ingredient.getName()),
            "INVENTORY"
        );
        
        return toDto(saved);
    }
    
    @Override
    public List<BatchDto> getBatchesByIngredient(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        return batchRepository.findByIngredientAndStatus(ingredient, IngredientBatch.BatchStatus.ACTIVE)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BatchDto> getExpiringBatches(String restaurantCode, Integer daysThreshold) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysThreshold != null ? daysThreshold : 7);

        return ingredientRepository
            .findByRestaurantAndExpiryDateBetweenOrderByExpiryDateAsc(restaurant, today, endDate)
            .stream()
            .map(ingredient -> {
                BatchDto dto = new BatchDto();
                dto.setId(ingredient.getId());
                dto.setIngredientId(ingredient.getId());
                dto.setIngredientName(ingredient.getName());
                dto.setBatchNumber("-");
                dto.setQuantity(ingredient.getCurrentStock());
                dto.setUnit(ingredient.getUnit());
                dto.setExpiryDate(ingredient.getExpiryDate());
                dto.setStatus("ACTIVE");

                long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, ingredient.getExpiryDate());
                dto.setDaysUntilExpiry(daysUntilExpiry);

                if (daysUntilExpiry < 0) {
                    dto.setExpiryRiskLevel("EXPIRED");
                } else if (daysUntilExpiry <= 2) {
                    dto.setExpiryRiskLevel("CRITICAL");
                } else if (daysUntilExpiry <= 7) {
                    dto.setExpiryRiskLevel("HIGH");
                } else if (daysUntilExpiry <= 14) {
                    dto.setExpiryRiskLevel("MEDIUM");
                } else {
                    dto.setExpiryRiskLevel("LOW");
                }

                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public void markBatchExpired(Long batchId) {
        IngredientBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        
        batch.setStatus(IngredientBatch.BatchStatus.EXPIRED);
        batchRepository.save(batch);
        
        // Log activity
        activityService.log(
            batch.getIngredient().getRestaurant().getCode(),
            Activity.Type.STOCK_UPDATED,
            String.format("Batch %s of %s marked as EXPIRED (%.2f %s wasted)",
                batch.getBatchNumber(), batch.getIngredient().getName(),
                batch.getQuantity(), batch.getUnit()),
            "SYSTEM"
        );
    }
    
    @Override
    public BatchDto getBatchById(Long batchId) {
        IngredientBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        return toDto(batch);
    }
    
    private String generateBatchNumber(Ingredient ingredient) {
        String prefix = ingredient.getName().substring(0, Math.min(3, ingredient.getName().length())).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return String.format("%s-%s", prefix, timestamp);
    }
    
    private BatchDto toDto(IngredientBatch batch) {
        BatchDto dto = new BatchDto();
        dto.setId(batch.getId());
        dto.setIngredientId(batch.getIngredient().getId());
        dto.setIngredientName(batch.getIngredient().getName());
        dto.setBatchNumber(batch.getBatchNumber());
        dto.setQuantity(batch.getQuantity());
        dto.setUnit(batch.getUnit());
        dto.setPurchaseDate(batch.getPurchaseDate());
        dto.setExpiryDate(batch.getExpiryDate());
        dto.setStatus(batch.getStatus().name());
        dto.setDaysUntilExpiry(batch.getDaysUntilExpiry());
        dto.setExpiryRiskLevel(batch.getExpiryRiskLevel());
        
        if (batch.getSupplier() != null) {
            dto.setSupplierId(batch.getSupplier().getId());
            dto.setSupplierName(batch.getSupplier().getName());
        }
        
        if (batch.getCostPerUnit() != null) {
            dto.setCostPerUnit(batch.getCostPerUnit());
        }
        
        return dto;
    }
}
