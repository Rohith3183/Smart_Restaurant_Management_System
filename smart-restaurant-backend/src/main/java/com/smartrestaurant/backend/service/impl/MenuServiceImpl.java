package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.*;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final ActivityService activityService;
    private final IngredientRepository ingredientRepository;

    public MenuServiceImpl(RestaurantRepository restaurantRepository,
                          MenuCategoryRepository categoryRepository,
                          MenuItemRepository menuItemRepository,
                          ActivityService activityService,
                          IngredientRepository ingredientRepository) {
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.activityService = activityService;
        this.ingredientRepository = ingredientRepository;
    }

    private Restaurant getRestaurant(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Override
    public List<MenuCategoryDto> getAllCategories(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        return categoryRepository.findByRestaurant(restaurant).stream()
                .map(cat -> new MenuCategoryDto(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public MenuCategoryDto createCategory(String restaurantCode, String name) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        MenuCategory category = MenuCategory.builder()
                .restaurant(restaurant)
                .name(name)
                .build();
        MenuCategory saved = categoryRepository.save(category);
        return new MenuCategoryDto(saved.getId(), saved.getName());
    }

    @Override
    public void deleteCategory(String restaurantCode, Long categoryId) {
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (!category.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Category does not belong to this restaurant");
        }
        categoryRepository.delete(category);
    }

    @Override
    public List<MenuItemDto> getAllMenuItems(String restaurantCode) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        refreshMenuAvailabilityForRestaurant(restaurant);
        return menuItemRepository.findByRestaurant(restaurant).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemDto createMenuItem(String restaurantCode, CreateMenuItemRequest request) {
        Restaurant restaurant = getRestaurant(restaurantCode);
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .price(request.getPrice())
                .available(request.isAvailable())
                .build();
        
        // Build ingredient mappings from request
        if (request.getMajorIngredients() != null && !request.getMajorIngredients().isEmpty()) {
            java.util.List<MenuItemIngredient> mappingList = request.getMajorIngredients().stream()
                    .map(reqIng -> {
                        Ingredient ing = ingredientRepository.findById(reqIng.getIngredientId())
                                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
                        MenuItemIngredient mi = new MenuItemIngredient();
                        mi.setMenuItem(item);
                        mi.setIngredient(ing);
                        mi.setQuantityPerItem(reqIng.getQuantityPerItem());
                        return mi;
                    })
                    .collect(java.util.stream.Collectors.toList());
            item.setIngredients(mappingList);
        }

        MenuItem saved = menuItemRepository.save(item);
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.MENU_CREATED,
        	    String.format("Menu item '%s' added in '%s' at ₹%s",
        	                  saved.getName(),
        	                  category.getName(),
        	                  saved.getPrice().toPlainString()),
        	    "INVENTORY"
        	);
        return toDto(saved);
    }

    @Override
    public MenuItemDto updateMenuItem(String restaurantCode, Long itemId, CreateMenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        if (!item.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Item does not belong to this restaurant");
        }

        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        BigDecimal oldPrice = item.getPrice();
        boolean oldAvailable = item.isAvailable();

        item.setCategory(category);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setAvailable(request.isAvailable());
        
     // Remove old ingredient mappings safely
        if (item.getIngredients() != null) {
            item.getIngredients().clear();
        }

        // Build new mappings list
        if (request.getMajorIngredients() != null && !request.getMajorIngredients().isEmpty()) {
            java.util.List<MenuItemIngredient> mappingList = request.getMajorIngredients().stream()
                    .map(reqIng -> {
                        Ingredient ing = ingredientRepository.findById(reqIng.getIngredientId())
                                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
                        MenuItemIngredient mi = new MenuItemIngredient();
                        mi.setMenuItem(item);
                        mi.setIngredient(ing);
                        mi.setQuantityPerItem(reqIng.getQuantityPerItem());
                        return mi;
                    })
                    .collect(java.util.stream.Collectors.toList());

            // Important: mutate existing collection, do NOT replace reference
            if (item.getIngredients() == null) {
                item.setIngredients(new java.util.ArrayList<>());
            }
            item.getIngredients().addAll(mappingList);
        }

        MenuItem updated = menuItemRepository.save(item);

        // price change log
        if (oldPrice != null && request.getPrice() != null && oldPrice.compareTo(request.getPrice()) != 0) {
            activityService.log(
                restaurantCode,
                Activity.Type.MENU_UPDATED,
                String.format("Price updated for '%s': %s → %s",
                              item.getName(),
                              oldPrice.toPlainString(),
                              item.getPrice().toPlainString()),
                "INVENTORY"
            );
        }

        // availability change log
        if (oldAvailable != item.isAvailable()) {
            activityService.log(
                restaurantCode,
                Activity.Type.MENU_UPDATED,
                String.format("'%s' marked %s",
                              item.getName(),
                              item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE"),
                "INVENTORY"
            );
        }

        return toDto(updated);
    }

    @Override
    public void deleteMenuItem(String restaurantCode, Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        
        if (!item.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Item does not belong to this restaurant");
        }

        menuItemRepository.delete(item);
    }

    private MenuItemDto toDto(MenuItem item) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(item.getId());
        dto.setCategoryId(item.getCategory().getId());
        dto.setCategoryName(item.getCategory().getName());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setAvailable(item.isAvailable());
        
        if (item.getIngredients() != null) {
            java.util.List<MenuItemDto.MenuItemIngredientView> list =
                    item.getIngredients().stream()
                            .map(mi -> {
                                MenuItemDto.MenuItemIngredientView v =
                                        new MenuItemDto.MenuItemIngredientView();
                                v.setIngredientId(mi.getIngredient().getId());
                                v.setIngredientName(mi.getIngredient().getName());
                                v.setQuantityPerItem(mi.getQuantityPerItem());
                                return v;
                            })
                            .collect(java.util.stream.Collectors.toList());
            dto.setIngredients(list);
        }
        
        return dto;
    }
    
 // Auto-update menu item availability based on ingredient expiry
    private void refreshMenuAvailabilityForRestaurant(Restaurant restaurant) {
        java.util.List<MenuItem> items = menuItemRepository.findByRestaurant(restaurant);

        for (MenuItem item : items) {
            boolean hasExpiredIngredient = false;

            if (item.getIngredients() != null && !item.getIngredients().isEmpty()) {
                for (MenuItemIngredient mi : item.getIngredients()) {
                    Ingredient ing = mi.getIngredient();
                    if (ing != null && ing.getExpiryDate() != null
                            && java.time.LocalDate.now().isAfter(ing.getExpiryDate())) {
                        hasExpiredIngredient = true;
                        break;
                    }
                }
            }

            // If any ingredient expired -> force unavailable
            if (hasExpiredIngredient) {
                item.setAvailable(false);
            } else {
                // If no expired ingredients, allow available again.
                // You can choose default true or respect previous manual choice.
                // To auto-enable when safe, use:
                item.setAvailable(true);
            }
        }

        menuItemRepository.saveAll(items);
    }
}
