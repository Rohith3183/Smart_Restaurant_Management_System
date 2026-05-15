package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Contact;
import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByRestaurantOrderByNameAsc(Restaurant restaurant);
}
