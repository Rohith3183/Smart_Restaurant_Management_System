package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.RestaurantRegistrationRequest;

import com.smartrestaurant.backend.dto.RestaurantDto;
import com.smartrestaurant.backend.dto.RestaurantResponse;
import com.smartrestaurant.backend.entity.Franchise;          // NEW
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.User;
import com.smartrestaurant.backend.repository.FranchiseRepository;   // NEW
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.UserRepository;
import com.smartrestaurant.backend.service.EmailService;
import com.smartrestaurant.backend.service.RestaurantService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FranchiseRepository franchiseRepository;   // NEW

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 FranchiseRepository franchiseRepository) {  // NEW
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.franchiseRepository = franchiseRepository;       // NEW
    }

    @Override
    public RestaurantResponse registerRestaurant(RestaurantRegistrationRequest request) {
        String code = request.getCode().toUpperCase().trim();

        if (restaurantRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Restaurant code already taken");
        }

        Franchise franchise = null;
        String restaurantNameToUse;

        // If registered under a franchise
        if (request.isFranchise()) {
            if (request.getFranchiseCode() == null || request.getFranchiseCode().isBlank()) {
                throw new IllegalArgumentException("Franchise code is required");
            }

            String fCode = request.getFranchiseCode().toUpperCase().trim();
            franchise = franchiseRepository.findByCodeIgnoreCase(fCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid franchise code"));

            // When under franchise, use franchise name as restaurant name
            restaurantNameToUse = franchise.getName();
        } else {
            // Normal restaurant registration
            restaurantNameToUse = request.getName();
        }

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantNameToUse)
                .code(code)
                .location(request.getLocation())
                .contact(request.getContact())
                .tableCount(request.getTableCount())
                .capacity(request.getCapacity())
                .enabled(true)
                .franchise(franchise)           // NEW: link to franchise if any
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);

        User owner = User.builder()
                .username(request.getOwnerUsername())
                .passwordHash(passwordEncoder.encode(request.getOwnerPassword()))
                .email(request.getOwnerEmail())
                .role(User.Role.OWNER)
                .restaurant(saved)
                .active(true)
                .build();

        userRepository.save(owner);

        // Send welcome email to restaurant owner (existing behaviour)
        try {
            emailService.sendRestaurantWelcomeEmail(
                    request.getOwnerEmail(),
                    saved.getName(),
                    saved.getCode(),
                    request.getOwnerUsername()
            );
        } catch (Exception e) {
            System.err.println("Failed to send restaurant welcome email: " + e.getMessage());
        }

        // If this restaurant is under a franchise, also notify franchise owner
        if (franchise != null) {
            try {
                emailService.sendNewRestaurantUnderFranchiseEmail(
                        franchise.getOwnerEmail(),
                        franchise.getName(),
                        saved.getCode()
                );
            } catch (Exception e) {
                System.err.println("Failed to notify franchise owner: " + e.getMessage());
            }
        }

        return toResponse(saved);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantDto getRestaurantByCode(String code) {
        Restaurant restaurant = restaurantRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        return toDto(restaurant);
    }
    
    @Override
    public List<RestaurantResponse> getRestaurantsByFranchise(String franchiseCode) {
        return restaurantRepository.findByFranchise_Code(franchiseCode.toUpperCase().trim())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        RestaurantResponse res = new RestaurantResponse();
        res.setId(restaurant.getId());
        res.setName(restaurant.getName());
        res.setCode(restaurant.getCode());
        res.setLocation(restaurant.getLocation());
        res.setContact(restaurant.getContact());
        res.setTableCount(restaurant.getTableCount());
        res.setCapacity(restaurant.getCapacity());
        return res;
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
