package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.BillDto;
import com.smartrestaurant.backend.dto.CreateBillRequest;
import com.smartrestaurant.backend.service.BillingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bills")
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // Accountant: Create bill for served order
    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<BillDto> createBill(@Valid @RequestBody CreateBillRequest request,
                                               HttpServletRequest httpRequest) {
        String restaurantCode = (String) httpRequest.getAttribute("restaurantCode");
        return ResponseEntity.ok(billingService.createBill(restaurantCode, request));
    }

    // Accountant: Get bill by order ID
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<BillDto> getBillByOrder(@PathVariable Long orderId,
                                                   HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(billingService.getBillByOrderId(restaurantCode, orderId));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ACCOUNTANT')")
    public ResponseEntity<java.util.List<BillDto>> getAllBills(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(billingService.getAllBills(restaurantCode));
    }
}
