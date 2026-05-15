package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;
import com.smartrestaurant.backend.service.ForecastingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forecast")
@CrossOrigin(origins = "*")
public class ForecastingController {

    private final ForecastingService forecastingService;

    public ForecastingController(ForecastingService forecastingService) {
        this.forecastingService = forecastingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','INVENTORY')")
    public ResponseEntity<List<PrepRecommendationDto>> forecast(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(forecastingService.forecastForDate(restaurantCode, date));
    }
}
