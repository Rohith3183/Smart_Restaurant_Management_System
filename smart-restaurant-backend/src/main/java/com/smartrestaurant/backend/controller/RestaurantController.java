package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.RestaurantRegistrationRequest;

import com.smartrestaurant.backend.dto.RestaurantResponse;
import com.smartrestaurant.backend.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping("/register")
    public ResponseEntity<RestaurantResponse> register(
            @Valid @RequestBody RestaurantRegistrationRequest request) {
        return ResponseEntity.ok(restaurantService.registerRestaurant(request));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAll() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }
}
