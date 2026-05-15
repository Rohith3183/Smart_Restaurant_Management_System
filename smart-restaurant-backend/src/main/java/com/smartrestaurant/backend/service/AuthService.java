package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.LoginRequest;
import com.smartrestaurant.backend.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
