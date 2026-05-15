package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.*;

import java.util.List;

public interface MenuService {
    // Categories
    List<MenuCategoryDto> getAllCategories(String restaurantCode);
    MenuCategoryDto createCategory(String restaurantCode, String name);
    void deleteCategory(String restaurantCode, Long categoryId);

    // Menu Items
    List<MenuItemDto> getAllMenuItems(String restaurantCode);
    MenuItemDto createMenuItem(String restaurantCode, CreateMenuItemRequest request);
    MenuItemDto updateMenuItem(String restaurantCode, Long itemId, CreateMenuItemRequest request);
    void deleteMenuItem(String restaurantCode, Long itemId);
}
