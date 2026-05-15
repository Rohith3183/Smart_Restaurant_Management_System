package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.PhysicalCountRequest;
import com.smartrestaurant.backend.dto.StockVarianceDto;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.StockVarianceService;
import com.smartrestaurant.backend.service.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockVarianceServiceImpl implements StockVarianceService {
    
    private final StockVarianceRepository varianceRepository;
    private final IngredientRepository ingredientRepository;
    private final RestaurantRepository restaurantRepository;
    private final ActivityService activityService;
    
    public StockVarianceServiceImpl(
            StockVarianceRepository varianceRepository,
            IngredientRepository ingredientRepository,
            RestaurantRepository restaurantRepository,
            ActivityService activityService) {
        this.varianceRepository = varianceRepository;
        this.ingredientRepository = ingredientRepository;
        this.restaurantRepository = restaurantRepository;
        this.activityService = activityService;
    }
    
    @Override
    public StockVarianceDto recordPhysicalCount(String restaurantCode, PhysicalCountRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        if (!ingredient.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Ingredient does not belong to this restaurant");
        }
        
        Double theoreticalStock = ingredient.getTheoreticalStock();
        Double physicalStock = request.getPhysicalStock();
        Double variance = physicalStock - theoreticalStock;
        Double variancePercentage = theoreticalStock != 0 ? 
            (variance / theoreticalStock) * 100 : 0.0;
        
        StockVariance.RiskLevel riskLevel = 
            StockVariance.calculateRiskLevel(variancePercentage);
        
        StockVariance stockVariance = StockVariance.builder()
            .ingredient(ingredient)
            .checkDate(LocalDate.now())
            .theoreticalStock(theoreticalStock)
            .physicalStock(physicalStock)
            .variance(variance)
            .variancePercentage(variancePercentage)
            .riskLevel(riskLevel)
            .notes(request.getNotes())
            .checkedBy(request.getCheckedBy())
            .build();
        
        StockVariance saved = varianceRepository.save(stockVariance);
        
        // Update ingredient current stock and reset theoretical stock
        ingredient.setCurrentStock(physicalStock);
        ingredient.setTheoreticalStock(physicalStock);
        ingredient.setLastPhysicalCountDate(LocalDate.now());
        ingredientRepository.save(ingredient);
        
        // Log activity with risk level
        String activityMessage = String.format(
            "Physical count: %s - Theoretical: %.2f, Physical: %.2f, Variance: %.2f%% (%s RISK)",
            ingredient.getName(), theoreticalStock, physicalStock, 
            variancePercentage, riskLevel.name()
        );
        
        activityService.log(
            restaurantCode,
            Activity.Type.STOCK_UPDATED,
            activityMessage,
            request.getCheckedBy()
        );
        
        return toDto(saved);
    }
    
    @Override
    public List<StockVarianceDto> getVarianceHistory(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        
        return varianceRepository.findByIngredientOrderByCheckDateDesc(ingredient)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockVarianceDto> getHighRiskVariances(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        return varianceRepository.findHighRiskVariances(restaurant)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockVarianceDto> getTodaysVariances(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        return varianceRepository.findByCheckDateAndIngredient_Restaurant(LocalDate.now(), restaurant)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    private StockVarianceDto toDto(StockVariance variance) {
        StockVarianceDto dto = new StockVarianceDto();
        dto.setId(variance.getId());
        dto.setIngredientId(variance.getIngredient().getId());
        dto.setIngredientName(variance.getIngredient().getName());
        dto.setCheckDate(variance.getCheckDate());
        dto.setTheoreticalStock(variance.getTheoreticalStock());
        dto.setPhysicalStock(variance.getPhysicalStock());
        dto.setVariance(variance.getVariance());
        dto.setVariancePercentage(variance.getVariancePercentage());
        dto.setRiskLevel(variance.getRiskLevel().name());
        dto.setNotes(variance.getNotes());
        dto.setCheckedBy(variance.getCheckedBy());
        return dto;
    }
}
