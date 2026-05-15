package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Investment;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    
    List<Investment> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
    
    @Query("SELECT SUM(i.amount) FROM Investment i " +
            "WHERE i.restaurant = :restaurant AND i.createdAt >= :startDate")
     Double getTotalInvestmentSince(@Param("restaurant") Restaurant restaurant,
                                    @Param("startDate") LocalDateTime startDate);

     @Query("SELECT SUM(i.amount) FROM Investment i " +
            "WHERE i.restaurant = :restaurant " +
            "AND i.createdAt >= :startDate " +
            "AND i.type <> com.smartrestaurant.backend.entity.Investment$Type.SALARY")
     Double getTotalNonSalaryInvestmentSince(@Param("restaurant") Restaurant restaurant,
                                             @Param("startDate") LocalDateTime startDate);
}
