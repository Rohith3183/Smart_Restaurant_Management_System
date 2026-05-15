// AlertRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Alert;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByRestaurantAndIsReadFalse(Restaurant restaurant);
    
    List<Alert> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
}
