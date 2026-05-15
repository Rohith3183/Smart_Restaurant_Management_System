package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.*;
import com.smartrestaurant.backend.entity.PasswordResetOTP;
import com.smartrestaurant.backend.entity.User;
import com.smartrestaurant.backend.repository.PasswordResetOTPRepository;
import com.smartrestaurant.backend.repository.UserRepository;
import com.smartrestaurant.backend.service.EmailService;
import com.smartrestaurant.backend.service.PasswordResetService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOTPRepository otpRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                   PasswordResetOTPRepository otpRepository,
                                   EmailService emailService,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordResetResponse sendOTP(ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findAll().stream()
                .filter(u -> request.getEmail().equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        // Check if user is OWNER
        if (user.getRole() != User.Role.OWNER) {
            throw new IllegalArgumentException("Password reset is only available for Owner accounts");
        }

        // Delete any existing OTPs for this email
        otpRepository.deleteByEmail(request.getEmail());

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Save OTP
        PasswordResetOTP otpEntity = PasswordResetOTP.builder()
                .email(request.getEmail())
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpRepository.save(otpEntity);

        // Send email
        emailService.sendPasswordResetOTP(request.getEmail(), otp);

        return new PasswordResetResponse("OTP sent to your email. Valid for 10 minutes.");
    }

    @Override
    public PasswordResetResponse verifyOTP(VerifyOTPRequest request) {
        PasswordResetOTP otpEntity = otpRepository.findByEmailAndUsedFalse(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        // Check if OTP matches
        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Check if expired
        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        // Get username
        User user = userRepository.findAll().stream()
                .filter(u -> request.getEmail().equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new PasswordResetResponse("OTP verified successfully", user.getUsername());
    }

    @Override
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        // Verify OTP first
        PasswordResetOTP otpEntity = otpRepository.findByEmailAndUsedFalse(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        // Find user and update password
        User user = userRepository.findAll().stream()
                .filter(u -> request.getEmail().equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        return new PasswordResetResponse("Password reset successfully");
    }
}
