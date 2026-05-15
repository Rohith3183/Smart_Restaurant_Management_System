package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.*;
import com.smartrestaurant.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password-reset")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<PasswordResetResponse> sendOTP(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.sendOTP(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<PasswordResetResponse> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        return ResponseEntity.ok(passwordResetService.verifyOTP(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }
}
