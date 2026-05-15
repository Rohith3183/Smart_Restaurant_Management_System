package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.RestaurantRegistrationRequest;
import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.dto.RestaurantResponse;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse registerRestaurant(RestaurantRegistrationRequest request);
    
 // RestaurantService.java
    RestaurantDto getRestaurantByCode(String code);

    List<RestaurantResponse> getAllRestaurants();
    
    List<RestaurantResponse> getRestaurantsByFranchise(String franchiseCode);
}
