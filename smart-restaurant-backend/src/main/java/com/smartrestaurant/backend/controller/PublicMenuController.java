package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.MenuItemDto;
import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.service.MenuService;
import com.smartrestaurant.backend.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/public")
@CrossOrigin(origins = "*")
public class PublicMenuController {

    private final MenuService menuService;
    private final RestaurantService restaurantService;

    public PublicMenuController(MenuService menuService, RestaurantService restaurantService) {
        this.menuService = menuService;
        this.restaurantService = restaurantService;
    }

    @GetMapping("/menu/{restaurantCode}")
    public ResponseEntity<Map<String, Object>> getPublicMenu(@PathVariable String restaurantCode) {
        RestaurantDto restaurant = restaurantService.getRestaurantByCode(restaurantCode);

        // uses same data Inventory updates, so always live
        List<MenuItemDto> items = menuService.getAllMenuItems(restaurantCode);

        Map<String, Object> response = new HashMap<>();
        response.put("restaurantName", restaurant.getName());
        response.put("contact", restaurant.getContact());
        response.put("location", restaurant.getLocation());
        response.put("items", items);

        return ResponseEntity.ok(response);
    }
}
