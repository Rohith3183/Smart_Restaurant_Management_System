// RestaurantTableRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.RestaurantTable;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurant(Restaurant restaurant);
}
