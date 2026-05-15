package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Bill;
import com.smartrestaurant.backend.entity.OrderEntity;
import com.smartrestaurant.backend.entity.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrder(OrderEntity order);
    
    List<Bill> findByOrder_RestaurantOrderByCreatedAtDesc(Restaurant restaurant);
}
	