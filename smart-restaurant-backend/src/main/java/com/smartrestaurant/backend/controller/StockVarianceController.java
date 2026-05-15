package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.StockVarianceDto;
import com.smartrestaurant.backend.dto.PhysicalCountRequest;
import com.smartrestaurant.backend.service.StockVarianceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-variance")
@CrossOrigin(origins = "*")
public class StockVarianceController {
    
    private final StockVarianceService varianceService;
    
    public StockVarianceController(StockVarianceService varianceService) {
        this.varianceService = varianceService;
    }
    
    @PostMapping("/physical-count")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER')")
    public ResponseEntity<StockVarianceDto> recordPhysicalCount(
            @Valid @RequestBody PhysicalCountRequest request,
            HttpServletRequest httpRequest) {
        String restaurantCode = (String) httpRequest.getAttribute("restaurantCode");
        return ResponseEntity.ok(varianceService.recordPhysicalCount(restaurantCode, request));
    }
    
    @GetMapping("/ingredient/{ingredientId}/history")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER')")
    public ResponseEntity<List<StockVarianceDto>> getVarianceHistory(
            @PathVariable Long ingredientId) {
        return ResponseEntity.ok(varianceService.getVarianceHistory(ingredientId));
    }
    
    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER')")
    public ResponseEntity<List<StockVarianceDto>> getHighRiskVariances(
            HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(varianceService.getHighRiskVariances(restaurantCode));
    }
    
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER')")
    public ResponseEntity<List<StockVarianceDto>> getTodaysVariances(
            HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(varianceService.getTodaysVariances(restaurantCode));
    }
}
