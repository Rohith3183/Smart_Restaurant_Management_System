package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findTop50ByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
    
    List<Activity> findByRestaurant(Restaurant restaurant);
    
    List<Activity> findByRestaurantAndTypeAndCreatedAtBetween(
            Restaurant restaurant,
            Activity.Type type,
            LocalDateTime start,
            LocalDateTime end
    );
}
