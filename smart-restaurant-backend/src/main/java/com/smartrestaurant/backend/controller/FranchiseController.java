package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.FranchiseRegistrationRequest;
import com.smartrestaurant.backend.dto.FranchiseResponse;
import com.smartrestaurant.backend.dto.LoginResponse;
import com.smartrestaurant.backend.dto.RestaurantResponse;
import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.dto.AiInsightDto;
import com.smartrestaurant.backend.dto.ChatRequestDto;
import com.smartrestaurant.backend.dto.ChatResponseDto;
import com.smartrestaurant.backend.service.FranchiseService;
import com.smartrestaurant.backend.service.FranchiseAnalyticsService;
import com.smartrestaurant.backend.service.FranchiseAiService;
import com.smartrestaurant.backend.service.RestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/franchises")
@CrossOrigin(origins = "*")
public class FranchiseController {

    private final FranchiseService franchiseService;
    private final RestaurantService restaurantService;
    private final FranchiseAnalyticsService franchiseAnalyticsService;
    private final FranchiseAiService franchiseAiService;

    public FranchiseController(FranchiseService franchiseService,
                               RestaurantService restaurantService,
                               FranchiseAnalyticsService franchiseAnalyticsService,
                               FranchiseAiService franchiseAiService) {
        this.franchiseService = franchiseService;
        this.restaurantService = restaurantService;
        this.franchiseAnalyticsService = franchiseAnalyticsService;
        this.franchiseAiService = franchiseAiService;
    }

    // Public: register franchise
    @PostMapping("/register")
    public ResponseEntity<FranchiseResponse> register(@Valid @RequestBody FranchiseRegistrationRequest request) {
        return ResponseEntity.ok(franchiseService.registerFranchise(request));
    }

    // Franchise owner: list restaurants
    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(HttpServletRequest request) {
        String franchiseCode = (String) request.getAttribute("franchiseCode");
        return ResponseEntity.ok(restaurantService.getRestaurantsByFranchise(franchiseCode));
    }

    // Franchise owner: combined analytics
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AnalyticsDto> getCombinedAnalytics(HttpServletRequest request) {
        String franchiseCode = (String) request.getAttribute("franchiseCode");
        return ResponseEntity.ok(franchiseAnalyticsService.getCombinedAnalytics(franchiseCode));
    }

    // Franchise owner: AI insights
    @GetMapping("/ai/sales-insights")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AiInsightDto> getAiSalesInsights(HttpServletRequest request) {
        String franchiseCode = (String) request.getAttribute("franchiseCode");
        return ResponseEntity.ok(franchiseAiService.generateInsights(franchiseCode));
    }

    @PostMapping("/ai/chat")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ChatResponseDto> chat(HttpServletRequest request,
                                                @RequestBody ChatRequestDto body) {
        String franchiseCode = (String) request.getAttribute("franchiseCode");
        return ResponseEntity.ok(franchiseAiService.chat(franchiseCode, body));
    }
    
    @PostMapping("/impersonate/{restaurantCode}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<LoginResponse> impersonateRestaurant(
            @PathVariable String restaurantCode,
            HttpServletRequest request
    ) {
        String franchiseCode = (String) request.getAttribute("franchiseCode");
        // validate that this restaurant belongs to this franchise
        LoginResponse response = franchiseAnalyticsService.impersonateRestaurant(franchiseCode, restaurantCode);
        return ResponseEntity.ok(response);
    }
}
