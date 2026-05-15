package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Customer;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByRestaurantAndPhone(Restaurant restaurant, String phone);
    List<Customer> findByRestaurant(Restaurant restaurant);
}
