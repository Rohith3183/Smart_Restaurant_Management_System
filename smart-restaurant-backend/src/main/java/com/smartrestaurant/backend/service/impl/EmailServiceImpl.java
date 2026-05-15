package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // plain text
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String restaurantName, String username) {
        String subject = "Welcome to Smart Restaurant System - " + restaurantName;
        String body =
                "Dear " + username + ",\n\n" +
                "Welcome to Smart Restaurant Management System!\n\n" +
                "Your restaurant '" + restaurantName + "' has been successfully registered.\n\n" +
                "You can now login and manage your restaurant operations.\n\n" +
                "Thank you for choosing our platform!\n\n" +
                "Best regards,\n" +
                "Smart Restaurant Team";
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetOTP(String email, String otp) {
        String subject = "Password Reset OTP - Smart Restaurant";
        String body =
                "Your password reset OTP is: " + otp + "\n\n" +
                "This OTP will expire in 10 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Smart Restaurant Team";
        sendEmail(email, subject, body);
    }

    @Override
    public void sendRestaurantWelcomeEmail(
            String ownerEmail,
            String restaurantName,
            String restaurantCode,
            String ownerUsername
    ) {
        String subject = "Welcome to Smart Restaurant - " + restaurantName;
        String body =
                "Hello " + ownerUsername + ",\n\n" +
                "Your restaurant has been registered successfully in Smart Restaurant System.\n\n" +
                "Restaurant Name: " + restaurantName + "\n" +
                "Restaurant Code: " + restaurantCode + "\n\n" +
                "You can login using:\n" +
                "  Code: " + restaurantCode + "\n" +
                "  Role: OWNER\n" +
                "  Username: " + ownerUsername + "\n\n" +
                "Regards,\nSmart Restaurant Team";
        sendEmail(ownerEmail, subject, body);
    }

    @Override
    public void sendFranchiseWelcomeEmail(
            String email,
            String franchiseName,
            String franchiseCode
    ) {
        String subject = "Welcome to Smart Restaurant Franchise";
        String body =
                "Hello " + franchiseName + ",\n\n" +
                "Your franchise has been created successfully in Smart Restaurant System.\n\n" +
                "Franchise Code: " + franchiseCode + "\n" +
                "To login use code: B-" + franchiseCode + "\n\n" +
                "When logging in, select role: OWNER and use your franchise username/password.\n\n" +
                "Regards,\nSmart Restaurant Team";
        sendEmail(email, subject, body);
    }

    @Override
    public void sendNewRestaurantUnderFranchiseEmail(
            String franchiseOwnerEmail,
            String franchiseName,
            String restaurantCode
    ) {
        String subject = "New Restaurant Registered Under Your Franchise";
        String body =
                "Hello " + franchiseName + ",\n\n" +
                "A new restaurant has been registered under your franchise.\n\n" +
                "Restaurant Code: " + restaurantCode + "\n\n" +
                "They will appear in your \"My Restaurants\" list in the Franchise Owner Dashboard.\n\n" +
                "Regards,\nSmart Restaurant Team";
        sendEmail(franchiseOwnerEmail, subject, body);
    }
}
