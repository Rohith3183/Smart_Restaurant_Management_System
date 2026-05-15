package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.*;
import com.smartrestaurant.backend.service.InventoryConsumptionService;
import com.smartrestaurant.backend.service.StockDeductionService;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ActivityService activityService;
    private final StockDeductionService stockDeductionService;
    private final InventoryConsumptionService inventoryConsumptionService;

    public OrderServiceImpl(RestaurantRepository restaurantRepository,
                           RestaurantTableRepository tableRepository,
                           MenuItemRepository menuItemRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           ActivityService activityService,
                           StockDeductionService stockDeductionService,
                           InventoryConsumptionService inventoryConsumptionService) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.activityService = activityService;
        this.stockDeductionService = stockDeductionService;
        this.inventoryConsumptionService = inventoryConsumptionService;
    }

    private Restaurant getRestaurant(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Override
    public OrderDto createOrder(String restaurantCode, CreateOrderRequest request) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        
        try {
        	
        RestaurantTable table = null;
        if ("DINE_IN".equals(request.getType())) {
            if (request.getTableId() == null) {
                throw new IllegalArgumentException("Table ID required for dine-in orders");
            }
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            if (table.getStatus() == RestaurantTable.Status.OCCUPIED) {
                throw new IllegalArgumentException("Table is already occupied");
            }

            table.setStatus(RestaurantTable.Status.OCCUPIED);
            tableRepository.save(table);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + menuItem.getName());
            }

            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .price(menuItem.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        OrderEntity order = OrderEntity.builder()
                .restaurant(restaurant)
                .table(table)
                .type(OrderEntity.Type.valueOf(request.getType()))
                .status(OrderEntity.Status.NEW)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        savedOrder.setItems(orderItems);
        
        inventoryConsumptionService.consumeIngredientsForOrder(restaurantCode, savedOrder.getId());
        
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.ORDER_CREATED,
        	    String.format("Order #%d created (%s%s) at %s – %d items",
        	    		savedOrder.getId(),
        	    		savedOrder.getType().name(),
        	    		savedOrder.getTable() != null ? ", Table " + savedOrder.getTable().getTableNumber() : "",
        	    		savedOrder.getCreatedAt().toLocalTime().toString(),
        	    		savedOrder.getItems().size()),
        	    "ACCOUNTANT"
        	);

        return toDto(savedOrder);
        }
        catch (Exception e) {
            // temporary debug log
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<OrderDto> getOrders(String restaurantCode, String status) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.valueOf(status))
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        
        return orderRepository.findAll().stream()
                .filter(o -> o.getRestaurant().getId().equals(restaurant.getId()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getKitchenOrders(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        List<OrderEntity> orders = new ArrayList<>();
        orders.addAll(orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.NEW));
        orders.addAll(orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.IN_KITCHEN));
        
        return orders.stream()
                .map(this::toDto)
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto startCooking(String restaurantCode, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }

        if (order.getStatus() != OrderEntity.Status.NEW) {
            throw new IllegalArgumentException("Order is not in NEW status");
        }

        order.setStatus(OrderEntity.Status.IN_KITCHEN);
        OrderEntity updated = orderRepository.save(order);
        return toDto(updated);
    }

    @Override
    public OrderDto markReady(String restaurantCode, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }

        if (order.getStatus() != OrderEntity.Status.IN_KITCHEN) {
            throw new IllegalArgumentException("Order is not in IN_KITCHEN status");
        }

        order.setStatus(OrderEntity.Status.READY);
        OrderEntity updated = orderRepository.save(order);
        return toDto(updated);
    }

    @Override
    public List<OrderDto> getReadyOrders(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.READY)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto markServed(String restaurantCode, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (!order.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }
        
        if (order.getStatus() != OrderEntity.Status.READY) {
            throw new IllegalArgumentException("Order is not in READY status");
        }
        
        order.setStatus(OrderEntity.Status.SERVED);
        OrderEntity updated = orderRepository.save(order);
        
        activityService.log(
            restaurantCode,
            Activity.Type.ORDER_STATUS_CHANGED,
            String.format("Order #%d served at %s", 
                order.getId(), LocalDateTime.now().toLocalTime().toString()),
            "WAITER"
        );
        
        return toDto(updated);
    }

    private OrderDto toDto(OrderEntity order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setType(order.getType().name());
        dto.setTableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null);
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    private OrderItemDto toItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setMenuItemName(item.getMenuItem().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
