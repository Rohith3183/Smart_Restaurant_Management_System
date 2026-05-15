package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.FranchiseRegistrationRequest;
import com.smartrestaurant.backend.dto.FranchiseResponse;
import com.smartrestaurant.backend.entity.Franchise;
import com.smartrestaurant.backend.repository.FranchiseRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.FranchiseService;
import com.smartrestaurant.backend.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FranchiseServiceImpl implements FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public FranchiseServiceImpl(FranchiseRepository franchiseRepository,
                                RestaurantRepository restaurantRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.franchiseRepository = franchiseRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public FranchiseResponse registerFranchise(FranchiseRegistrationRequest request) {

        String code = request.getCode().toUpperCase().trim();

        if (restaurantRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Code already used by a restaurant");
        }
        if (franchiseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Franchise code already exists");
        }
        if (franchiseRepository.existsByOwnerUsername(request.getOwnerUsername())) {
            throw new IllegalArgumentException("Username already used by another franchise");
        }
        if (franchiseRepository.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new IllegalArgumentException("Email already used by another franchise");
        }

        Franchise franchise = Franchise.builder()
                .code(code)
                .name(request.getName())
                .ownerUsername(request.getOwnerUsername())
                .ownerPasswordHash(passwordEncoder.encode(request.getOwnerPassword()))
                .ownerEmail(request.getOwnerEmail())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        Franchise saved = franchiseRepository.save(franchise);

     // send welcome email with B-code
        try {
            emailService.sendFranchiseWelcomeEmail(
                    saved.getOwnerEmail(),
                    saved.getName(),
                    saved.getCode()
            );
        } catch (Exception ex) {
            // log error but don't break registration
            System.err.println("Failed to send franchise welcome email: " + ex.getMessage());
        }

        FranchiseResponse resp = new FranchiseResponse();
        resp.setId(saved.getId());
        resp.setCode(saved.getCode());
        resp.setName(saved.getName());
        resp.setOwnerEmail(saved.getOwnerEmail());
        resp.setEnabled(saved.isEnabled());
        return resp;
    }
}
