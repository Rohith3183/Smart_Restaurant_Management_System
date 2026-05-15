package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//SupplierRepository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
 
 List<Supplier> findByRestaurantAndIsActive(Restaurant restaurant, Boolean isActive);
 
 @Query("SELECT s FROM Supplier s WHERE s.restaurant = :restaurant " +
        "AND s.isActive = true ORDER BY s.rating DESC, s.onTimeDeliveries DESC")
 List<Supplier> findTopSuppliers(@Param("restaurant") Restaurant restaurant);
}