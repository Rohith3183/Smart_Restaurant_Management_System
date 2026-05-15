// MenuItemIngredientRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.MenuItemIngredient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {
}
