package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.*;

public interface PasswordResetService {
    PasswordResetResponse sendOTP(ForgotPasswordRequest request);
    PasswordResetResponse verifyOTP(VerifyOTPRequest request);
    PasswordResetResponse resetPassword(ResetPasswordRequest request);
}
