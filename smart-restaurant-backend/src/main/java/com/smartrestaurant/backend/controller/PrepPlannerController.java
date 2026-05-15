package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.PrepRecommendationDto;
import com.smartrestaurant.backend.service.PrepPlannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prep-plan")
@CrossOrigin(origins = "*")
public class PrepPlannerController {

    private final PrepPlannerService prepPlannerService;

    public PrepPlannerController(PrepPlannerService prepPlannerService) {
        this.prepPlannerService = prepPlannerService;
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('OWNER','CHEF','INVENTORY')")
    public ResponseEntity<List<PrepRecommendationDto>> getTodayPlan(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(prepPlannerService.getTodayPrepPlan(restaurantCode));
    }
}
