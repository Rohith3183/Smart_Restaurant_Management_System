package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.dto.FranchiseAdminDto;

import java.util.List;

public interface RootAdminService {
    List<RestaurantDto> getAllRestaurants();
    void enableRestaurant(Long restaurantId);
    void disableRestaurant(Long restaurantId);
    void deleteRestaurant(Long restaurantId);
    
    List<FranchiseAdminDto> getAllFranchises();
    void enableFranchise(Long franchiseId);
    void disableFranchise(Long franchiseId);
    void deleteFranchise(Long franchiseId);
}
