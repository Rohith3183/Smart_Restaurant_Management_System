package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.CreateOrderRequest;
import com.smartrestaurant.backend.dto.OrderDto;

import java.util.List;

public interface OrderService {
    // Accountant: create order
    OrderDto createOrder(String restaurantCode, CreateOrderRequest request);

    // Accountant: view all orders or filter by status
    List<OrderDto> getOrders(String restaurantCode, String status);

    // Chef: get NEW and IN_KITCHEN orders
    List<OrderDto> getKitchenOrders(String restaurantCode);

    // Chef: mark order as IN_KITCHEN
    OrderDto startCooking(String restaurantCode, Long orderId);

    // Chef: mark order as READY
    OrderDto markReady(String restaurantCode, Long orderId);

    // Waiter: get READY orders
    List<OrderDto> getReadyOrders(String restaurantCode);

    // Waiter: mark order as SERVED
    OrderDto markServed(String restaurantCode, Long orderId);
}
