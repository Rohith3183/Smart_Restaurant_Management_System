package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.BatchDto;
import com.smartrestaurant.backend.dto.CreateBatchRequest;
import com.smartrestaurant.backend.service.BatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@CrossOrigin(origins = "*")
public class BatchController {
    
    private final BatchService batchService;
    
    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<BatchDto> createBatch(
            @Valid @RequestBody CreateBatchRequest request,
            HttpServletRequest httpRequest) {
        String restaurantCode = (String) httpRequest.getAttribute("restaurantCode");
        return ResponseEntity.ok(batchService.createBatch(restaurantCode, request));
    }
    
    @GetMapping("/ingredient/{ingredientId}")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER', 'CHEF')")
    public ResponseEntity<List<BatchDto>> getBatchesByIngredient(
            @PathVariable Long ingredientId) {
        return ResponseEntity.ok(batchService.getBatchesByIngredient(ingredientId));
    }
    
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('INVENTORY', 'OWNER')")
    public ResponseEntity<List<BatchDto>> getExpiringBatches(
            @RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(batchService.getExpiringBatches(restaurantCode, days));
    }
    
    @PutMapping("/{batchId}/mark-expired")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<Void> markExpired(@PathVariable Long batchId) {
        batchService.markBatchExpired(batchId);
        return ResponseEntity.noContent().build();
    }
}
