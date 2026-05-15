package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.*;
import com.smartrestaurant.backend.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // Categories
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER', 'ACCOUNTANT', 'INVENTORY')")
    public ResponseEntity<List<MenuCategoryDto>> getAllCategories(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(menuService.getAllCategories(restaurantCode));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<MenuCategoryDto> createCategory(@RequestBody Map<String, String> body, 
                                                          HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(menuService.createCategory(restaurantCode, body.get("name")));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        menuService.deleteCategory(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }

    // Menu Items
    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('OWNER', 'ACCOUNTANT', 'INVENTORY', 'WAITER')")
    public ResponseEntity<List<MenuItemDto>> getAllItems(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(menuService.getAllMenuItems(restaurantCode));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<MenuItemDto> createItem(@Valid @RequestBody CreateMenuItemRequest req,
                                                   HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(menuService.createMenuItem(restaurantCode, req));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<MenuItemDto> updateItem(@PathVariable Long id,
                                                   @Valid @RequestBody CreateMenuItemRequest req,
                                                   HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(menuService.updateMenuItem(restaurantCode, id, req));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        menuService.deleteMenuItem(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }
}
