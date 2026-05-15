package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.PasswordResetOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, Long> {
    
    Optional<PasswordResetOTP> findByEmailAndUsedFalse(String email);
    
    void deleteByEmail(String email);
}
