package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.dto.FranchiseAdminDto;
import com.smartrestaurant.backend.entity.Franchise;
import com.smartrestaurant.backend.repository.FranchiseRepository;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.RootAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RootAdminServiceImpl implements RootAdminService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final IngredientRepository ingredientRepository;
    private final RestaurantTableRepository tableRepository;
    private final AlertRepository alertRepository;
    private final InvestmentRepository investmentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final BillRepository billRepository;
    private final FranchiseRepository franchiseRepository;
    private final CustomerRepository customerRepository;
    private final ActivityRepository activityRepository;

    public RootAdminServiceImpl(RestaurantRepository restaurantRepository,
                               UserRepository userRepository,
                               EmployeeRepository employeeRepository,
                               OrderRepository orderRepository,
                               MenuItemRepository menuItemRepository,
                               IngredientRepository ingredientRepository,
                               RestaurantTableRepository tableRepository,
                               AlertRepository alertRepository,
                               InvestmentRepository investmentRepository,
                               SalaryPaymentRepository salaryPaymentRepository,
                               MenuCategoryRepository menuCategoryRepository,
                               BillRepository billRepository,
                               FranchiseRepository franchiseRepository,
                               CustomerRepository customerRepository,
                               ActivityRepository activityRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.ingredientRepository = ingredientRepository;
        this.tableRepository = tableRepository;
        this.alertRepository = alertRepository;
        this.investmentRepository = investmentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.billRepository = billRepository;
        this.franchiseRepository = franchiseRepository;
        this.customerRepository = customerRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .filter(r -> !r.getCode().equals("ROOT3183"))  // Exclude ROOT admin
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void enableRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        restaurant.setEnabled(true);
        restaurantRepository.save(restaurant);
    }

    @Override
    public void disableRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        restaurant.setEnabled(false);
        restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Prevent deleting ROOT admin
        if (restaurant.getCode().equals("ROOT3183")) {
            throw new IllegalArgumentException("Cannot delete ROOT admin");
        }

        // Delete all related data (CASCADE)
        alertRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).forEach(alertRepository::delete);
        salaryPaymentRepository.findAll().stream()
                .filter(s -> s.getEmployee().getRestaurant().getId().equals(restaurantId))
                .forEach(salaryPaymentRepository::delete);
        investmentRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).forEach(investmentRepository::delete);
        orderRepository.findByRestaurant(restaurant).forEach(order -> {
            billRepository.findByOrder(order).ifPresent(billRepository::delete);
        });
        orderRepository.findByRestaurant(restaurant).forEach(orderRepository::delete);
        menuItemRepository.findByRestaurant(restaurant).forEach(menuItemRepository::delete);
        ingredientRepository.findByRestaurant(restaurant).forEach(ingredientRepository::delete);
        employeeRepository.findByRestaurant(restaurant).forEach(employeeRepository::delete);
        tableRepository.findByRestaurant(restaurant).forEach(tableRepository::delete);
        menuCategoryRepository.findByRestaurant(restaurant)
        .forEach(menuCategoryRepository::delete);
        customerRepository.findByRestaurant(restaurant)
        .forEach(customerRepository::delete);
        activityRepository.findByRestaurant(restaurant)
        .forEach(activityRepository::delete);
        userRepository.findAll().stream()
                .filter(u -> u.getRestaurant().getId().equals(restaurantId))
                .forEach(userRepository::delete);

        restaurantRepository.delete(restaurant);
    }
    
    @Override
    public List<FranchiseAdminDto> getAllFranchises() {
        return franchiseRepository.findAll().stream()
                .map(this::toFranchiseDto)
                .toList();
    }

    @Override
    public void enableFranchise(Long franchiseId) {
        Franchise f = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new IllegalArgumentException("Franchise not found"));
        f.setEnabled(true);
        franchiseRepository.save(f);
    }

    @Override
    public void disableFranchise(Long franchiseId) {
        Franchise f = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new IllegalArgumentException("Franchise not found"));
        f.setEnabled(false);
        franchiseRepository.save(f);
    }
    
    @Override
    public void deleteFranchise(Long franchiseId) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new IllegalArgumentException("Franchise not found"));

        // Delete all restaurants under this franchise using existing delete logic
        List<Restaurant> restaurants = restaurantRepository.findByFranchise(franchise);
        for (Restaurant r : restaurants) {
            deleteRestaurant(r.getId());  // reuse existing method that cascades deletions
        }

        franchiseRepository.delete(franchise);
    }

    
    private FranchiseAdminDto toFranchiseDto(Franchise f) {
        FranchiseAdminDto dto = new FranchiseAdminDto();
        dto.setId(f.getId());
        dto.setCode(f.getCode());
        dto.setName(f.getName());
        dto.setOwnerUsername(f.getOwnerUsername());
        dto.setOwnerEmail(f.getOwnerEmail());
        dto.setEnabled(f.isEnabled());
        return dto;
    }

    private RestaurantDto toDto(Restaurant restaurant) {
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setCode(restaurant.getCode());
        dto.setLocation(restaurant.getLocation());
        dto.setContact(restaurant.getContact());
        dto.setTableCount(restaurant.getTableCount());
        dto.setCapacity(restaurant.getCapacity());
        dto.setEnabled(restaurant.isEnabled());
        return dto;
    }
}
