package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.AlertDto;
import com.smartrestaurant.backend.dto.CreateAlertRequest;
import com.smartrestaurant.backend.service.AlertService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'CHEF', 'WAITER', 'INVENTORY')")
    public ResponseEntity<AlertDto> createAlert(@Valid @RequestBody CreateAlertRequest request,
                                                 HttpServletRequest httpRequest) {
        String restaurantCode = (String) httpRequest.getAttribute("restaurantCode");
        return ResponseEntity.ok(alertService.createAlert(restaurantCode, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<AlertDto>> getUnreadAlerts(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        alertService.checkLowStock(restaurantCode);  // Auto-check when owner views
        return ResponseEntity.ok(alertService.getUnreadAlerts(restaurantCode));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        alertService.markAsRead(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }
}
