package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.dto.LoginResponse;

public interface FranchiseAnalyticsService {

    // Combined analytics across all restaurants of a franchise
    AnalyticsDto getCombinedAnalytics(String franchiseCode);

    // For impersonation: issue a normal owner token for a specific restaurant
    LoginResponse impersonateRestaurant(String franchiseCode, String restaurantCode);
}
