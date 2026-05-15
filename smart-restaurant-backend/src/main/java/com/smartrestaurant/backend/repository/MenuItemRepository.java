// MenuItemRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.MenuItem;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);
}
