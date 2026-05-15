// OrderItemRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
