package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.IngredientDto;
import com.smartrestaurant.backend.dto.IngredientWithBatchesDto;
import com.smartrestaurant.backend.service.IngredientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'CHEF', 'INVENTORY')")
    public ResponseEntity<List<IngredientDto>> getAll(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(ingredientService.getAllIngredients(restaurantCode));
    }

    @PostMapping
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<IngredientDto> create(@RequestBody IngredientDto dto, 
                                                 HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(ingredientService.createIngredient(restaurantCode, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<IngredientDto> update(@PathVariable Long id,
                                                 @RequestBody IngredientDto dto,
                                                 HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(ingredientService.updateIngredient(restaurantCode, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        ingredientService.deleteIngredient(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/with-batches")
    @PreAuthorize("hasAnyRole('OWNER', 'CHEF', 'INVENTORY')")
    public ResponseEntity<java.util.List<IngredientWithBatchesDto>> getWithBatches(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(ingredientService.getIngredientsWithBatches(restaurantCode));
    }
}
