package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.InventoryConsumptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryConsumptionServiceImpl implements InventoryConsumptionService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryConsumptionServiceImpl(RestaurantRepository restaurantRepository,
                                           OrderRepository orderRepository,
                                           MenuItemRepository menuItemRepository,
                                           IngredientRepository ingredientRepository) {
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    public void consumeIngredientsForOrder(String restaurantCode, Long orderId) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }

        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        for (OrderItem orderItem : items) {
            MenuItem menuItem = orderItem.getMenuItem();
            if (menuItem == null) continue;

            // Ensure we have ingredients loaded
            MenuItem fullItem = menuItemRepository.findById(menuItem.getId())
                    .orElse(menuItem);

            if (fullItem.getIngredients() == null || fullItem.getIngredients().isEmpty()) continue;

            int qty = orderItem.getQuantity() == null ? 0 : orderItem.getQuantity();

            for (MenuItemIngredient mi : fullItem.getIngredients()) {
                Ingredient ing = mi.getIngredient();
                if (ing == null) continue;

                if (!ing.getRestaurant().getId().equals(restaurant.getId())) {
                    continue;
                }

                Double perItemQty = mi.getQuantityPerItem();
                if (perItemQty == null) continue;

                double totalToDeduct = perItemQty * qty;

                Double current = ing.getCurrentStock();
                if (current == null) current = 0.0;

                ing.setCurrentStock(current - totalToDeduct);

                ingredientRepository.save(ing);
            }
        }
    }
}
