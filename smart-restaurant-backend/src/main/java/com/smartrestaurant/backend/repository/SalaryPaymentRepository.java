package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.Employee;
import com.smartrestaurant.backend.entity.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {
    
    List<SalaryPayment> findByEmployeeOrderByPaymentDateDesc(Employee employee);
    
    Optional<SalaryPayment> findByEmployeeAndMonth(Employee employee, String month);
}
