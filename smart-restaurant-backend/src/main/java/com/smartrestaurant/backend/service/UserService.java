package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.CreateUserRequest;
import com.smartrestaurant.backend.dto.UpdateUserRequest;
import com.smartrestaurant.backend.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(String restaurantCode, CreateUserRequest request);
    UserDto updateUser(String restaurantCode, Long userId, UpdateUserRequest request);
    List<UserDto> getAllUsers(String restaurantCode);
    void deleteUser(String restaurantCode, Long userId);
}
