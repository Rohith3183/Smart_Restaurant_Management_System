package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Franchise;
import com.smartrestaurant.backend.entity.Restaurant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByCode(String code);

    boolean existsByCode(String code);
    
    List<Restaurant> findByFranchise_Code(String code);
    List<Restaurant> findByFranchise(Franchise franchise);
}
	