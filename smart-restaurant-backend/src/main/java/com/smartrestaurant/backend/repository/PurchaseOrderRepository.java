package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//PurchaseOrderRepository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
 
 List<PurchaseOrder> findByRestaurantAndStatus(Restaurant restaurant, PurchaseOrder.POStatus status);
 
 @Query("SELECT po FROM PurchaseOrder po WHERE po.restaurant = :restaurant " +
        "ORDER BY po.createdAt DESC")
 List<PurchaseOrder> findRecentPurchaseOrders(@Param("restaurant") Restaurant restaurant);
}