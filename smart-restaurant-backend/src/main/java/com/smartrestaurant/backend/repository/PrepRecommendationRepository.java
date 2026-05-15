package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//PrepRecommendationRepository
public interface PrepRecommendationRepository extends JpaRepository<PrepRecommendation, Long> {
 
 List<PrepRecommendation> findByRestaurantAndRecommendationDate(Restaurant restaurant, LocalDate date);
 
 @Query("SELECT pr FROM PrepRecommendation pr WHERE pr.restaurant = :restaurant " +
        "AND pr.recommendationDate = :date " +
        "ORDER BY pr.predictedRequirement DESC")
 List<PrepRecommendation> findTodaysRecommendations(
     @Param("restaurant") Restaurant restaurant,
     @Param("date") LocalDate date
 );
}