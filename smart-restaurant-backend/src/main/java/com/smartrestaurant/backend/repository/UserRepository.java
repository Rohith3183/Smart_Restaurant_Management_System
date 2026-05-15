package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByRestaurant_IdAndRole(Long restaurantId, User.Role role);
}
