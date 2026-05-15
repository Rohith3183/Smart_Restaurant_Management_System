// MenuCategoryRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.MenuCategory;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findByRestaurant(Restaurant restaurant);
}
