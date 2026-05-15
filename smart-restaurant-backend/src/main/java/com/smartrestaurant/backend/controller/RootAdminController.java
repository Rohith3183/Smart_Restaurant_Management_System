package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.dto.FranchiseAdminDto;
import com.smartrestaurant.backend.service.RootAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/root")
@CrossOrigin(origins = "*")
public class RootAdminController {

    private final RootAdminService rootAdminService;

    public RootAdminController(RootAdminService rootAdminService) {
        this.rootAdminService = rootAdminService;
    }

    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        
        // Only ROOT admin can access
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(rootAdminService.getAllRestaurants());
    }

    @PutMapping("/restaurants/{id}/enable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> enableRestaurant(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        
        rootAdminService.enableRestaurant(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/restaurants/{id}/disable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> disableRestaurant(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        
        rootAdminService.disableRestaurant(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/restaurants/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        
        rootAdminService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/franchises")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<FranchiseAdminDto>> getAllFranchises(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(rootAdminService.getAllFranchises());
    }

    @PutMapping("/franchises/{id}/enable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> enableFranchise(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        rootAdminService.enableFranchise(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/franchises/{id}/disable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> disableFranchise(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        rootAdminService.disableFranchise(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/franchises/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteFranchise(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        if (!"ROOT3183".equals(restaurantCode)) {
            return ResponseEntity.status(403).build();
        }
        rootAdminService.deleteFranchise(id);
        return ResponseEntity.noContent().build();
    }
}
