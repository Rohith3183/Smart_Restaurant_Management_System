package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.LoginRequest;
import com.smartrestaurant.backend.dto.LoginResponse;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.entity.Franchise;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.User;
import com.smartrestaurant.backend.repository.FranchiseRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.UserRepository;
import com.smartrestaurant.backend.security.jwt.JwtTokenProvider;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ActivityService activityService;
    private final FranchiseRepository franchiseRepository;

    public AuthServiceImpl(RestaurantRepository restaurantRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           ActivityService activityService,
                           FranchiseRepository franchiseRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.activityService = activityService;
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        String rawCode = request.getRestaurantCode().trim();
        String upperCode = rawCode.toUpperCase();

        // =============== FRANCHISE LOGIN: B-<franchiseCode> ONLY ===============
        if (upperCode.startsWith("B-")) {
            String franchiseCode = upperCode.substring(2).trim(); // after "B-"

            System.out.println("Franchise login attempt: rawCode=" + rawCode +
                    ", upperCode=" + upperCode + ", franchiseCode=" + franchiseCode);

            Franchise franchise = franchiseRepository.findByCodeIgnoreCase(franchiseCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Restaurant not found with code " + rawCode));

            if (!franchise.isEnabled()) {
                throw new IllegalArgumentException(
                        "Your franchise access has been disabled. Please contact support.");
            }

            // only OWNER allowed for franchise login
            if (!"OWNER".equals(request.getRole())) {
                throw new IllegalArgumentException("Role mismatch");
            }

            if (!franchise.getOwnerUsername().equals(request.getUsername())) {
                throw new IllegalArgumentException("User not found " + request.getUsername());
            }

            if (!passwordEncoder.matches(request.getPassword(), franchise.getOwnerPasswordHash())) {
                throw new IllegalArgumentException("Invalid password");
            }

            // Use normal token, restaurantCode in token = B-<franchiseCode>
            String token = jwtTokenProvider.createToken(
                    franchise.getOwnerUsername(),
                    "OWNER",
                    upperCode
            );

            return new LoginResponse(
                    token,
                    franchise.getOwnerUsername(),
                    "OWNER",
                    upperCode,
                    franchise.getName()
            );
        }

        // =============== NORMAL RESTAURANT LOGIN (no B-) ===============

        // 1. Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findByCode(upperCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Restaurant not found with code " + request.getRestaurantCode()));

        // CHECK IF RESTAURANT IS ENABLED
        if (!restaurant.isEnabled()) {
            throw new IllegalArgumentException(
                    "Your account access has been temporarily disabled due to unpaid service fees. " +
                            "Please contact support to restore access.");
        }

        // 2. Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found " + request.getUsername()));

        // 3. Verify user belongs to this restaurant
        if (!user.getRestaurant().getId().equals(restaurant.getId())) {
            throw new IllegalArgumentException("User does not belong to this restaurant");
        }

        // 4. Verify role matches
        if (!user.getRole().name().equals(request.getRole())) {
            throw new IllegalArgumentException("Role mismatch");
        }

        // 5. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // 6. Check if user is active
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        // 7. Generate JWT token
        String token = jwtTokenProvider.createToken(
                user.getUsername(),
                user.getRole().name(),
                restaurant.getCode()
        );

        try {
            activityService.log(
                    restaurant.getCode(),
                    Activity.Type.LOGIN,
                    String.format("%s %s logged in at %s",
                            user.getRole().name().charAt(0)
                                    + user.getRole().name().substring(1).toLowerCase(),
                            user.getUsername(),
                            LocalDateTime.now().toLocalTime()),
                    user.getUsername()
            );
        } catch (IllegalArgumentException ex) {
            activityService.log(
                    request.getRestaurantCode(),
                    Activity.Type.LOGIN_FAILED,
                    String.format("Failed login attempt for username '%s' at %s",
                            request.getUsername(),
                            LocalDateTime.now().toLocalTime()),
                    request.getUsername()
            );
            throw ex;
        }

        String headerName = restaurant.getFranchise() != null
                ? restaurant.getFranchise().getName()
                : restaurant.getName();

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                restaurant.getCode(),
                headerName
        );
    }
}
