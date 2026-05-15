package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.InventoryMetricsDto;
import com.smartrestaurant.backend.service.InventoryDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory-dashboard")
@CrossOrigin(origins = "*")
public class InventoryDashboardController {

    private final InventoryDashboardService inventoryDashboardService;

    public InventoryDashboardController(InventoryDashboardService inventoryDashboardService) {
        this.inventoryDashboardService = inventoryDashboardService;
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('OWNER','INVENTORY')")
    public ResponseEntity<InventoryMetricsDto> getMetrics(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(inventoryDashboardService.getMetrics(restaurantCode));
    }
}
