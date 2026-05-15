package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.CreateOrderRequest;
import com.smartrestaurant.backend.dto.OrderDto;
import com.smartrestaurant.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Accountant: Create order
    @PostMapping
    @PreAuthorize("hasAnyRole('ACCOUNTANT','WAITER')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                 HttpServletRequest httpRequest) {
        String restaurantCode = (String) httpRequest.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.createOrder(restaurantCode, request));
    }

    // Accountant: Get all orders or filter by status
    @GetMapping
    @PreAuthorize("hasAnyRole('ACCOUNTANT','WAITER')")
    public ResponseEntity<List<OrderDto>> getOrders(@RequestParam(required = false) String status,
                                                     HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.getOrders(restaurantCode, status));
    }

    // Chef: Get kitchen orders (NEW + IN_KITCHEN)
    @GetMapping("/kitchen")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<List<OrderDto>> getKitchenOrders(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.getKitchenOrders(restaurantCode));
    }

    // Chef: Start cooking (NEW -> IN_KITCHEN)
    @PutMapping("/{id}/start-cooking")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<OrderDto> startCooking(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.startCooking(restaurantCode, id));
    }

    // Chef: Mark as ready (IN_KITCHEN -> READY)
    @PutMapping("/{id}/mark-ready")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<OrderDto> markReady(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.markReady(restaurantCode, id));
    }

    // Waiter: Get ready orders
    @GetMapping("/ready")
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<List<OrderDto>> getReadyOrders(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.getReadyOrders(restaurantCode));
    }

    // Waiter: Mark as served (READY -> SERVED)
    @PutMapping("/{id}/mark-served")
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<OrderDto> markServed(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(orderService.markServed(restaurantCode, id));
    }
}
