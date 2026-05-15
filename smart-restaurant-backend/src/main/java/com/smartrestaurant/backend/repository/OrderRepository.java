package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.OrderEntity;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.OrderEntity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByRestaurantAndStatus(Restaurant restaurant, Status status);
    
    List<OrderEntity> findByRestaurant(Restaurant restaurant);
    
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.restaurant = :restaurant AND o.status = 'CLOSED' AND o.createdAt >= :startDate")
    Double getTotalRevenueSince(@Param("restaurant") Restaurant restaurant, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.restaurant = :restaurant AND o.status = 'CLOSED'")
    Long getTotalCompletedOrders(@Param("restaurant") Restaurant restaurant);
}
