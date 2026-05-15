package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Feedback;
import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByRestaurant(Restaurant restaurant);

    // counts for each star (1..5) for each feature
    @Query("""
        SELECT f.food AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.food
        """)
    List<Object[]> countFoodRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.ambiance AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.ambiance
        """)
    List<Object[]> countAmbianceRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.ingredients AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.ingredients
        """)
    List<Object[]> countIngredientsRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.service AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.service
        """)
    List<Object[]> countServiceRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.cleanliness AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.cleanliness
        """)
    List<Object[]> countCleanlinessRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.valueForMoney AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.valueForMoney
        """)
    List<Object[]> countValueRatings(@Param("restaurant") Restaurant restaurant);

    @Query("""
        SELECT f.overall AS rating, COUNT(f) AS cnt
        FROM Feedback f
        WHERE f.restaurant = :restaurant
        GROUP BY f.overall
        """)
    List<Object[]> countOverallRatings(@Param("restaurant") Restaurant restaurant);
}
