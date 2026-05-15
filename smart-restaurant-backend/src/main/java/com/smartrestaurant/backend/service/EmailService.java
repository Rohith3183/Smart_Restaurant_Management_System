package com.smartrestaurant.backend.service;

public interface EmailService {

    void sendWelcomeEmail(String to, String restaurantName, String username);

    void sendPasswordResetOTP(String email, String otp);

    void sendEmail(String to, String subject, String body);

    void sendRestaurantWelcomeEmail(
            String ownerEmail,
            String restaurantName,
            String restaurantCode,
            String ownerUsername
    );

    void sendFranchiseWelcomeEmail(
            String email,
            String franchiseName,
            String franchiseCode
    );

    void sendNewRestaurantUnderFranchiseEmail(
            String franchiseOwnerEmail,
            String franchiseName,
            String restaurantCode
    );
}
