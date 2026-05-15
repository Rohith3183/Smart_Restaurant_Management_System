// EmployeeRepository
package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Employee;

import com.smartrestaurant.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByRestaurant(Restaurant restaurant);
}
