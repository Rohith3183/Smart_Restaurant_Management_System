package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

    boolean existsByCode(String code);

    boolean existsByOwnerUsername(String ownerUsername);

    boolean existsByOwnerEmail(String ownerEmail);

    Optional<Franchise> findByCodeIgnoreCase(String code);
}

