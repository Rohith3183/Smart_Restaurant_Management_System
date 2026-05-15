package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.SalaryDto;
import com.smartrestaurant.backend.service.SalaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salaries")
@CrossOrigin(origins = "*")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<SalaryDto>> getAll(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(salaryService.getAllSalaries(restaurantCode));
    }

    @PostMapping("/{employeeId}/pay")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> paySalary(@PathVariable Long employeeId, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        salaryService.paySalary(restaurantCode, employeeId);
        return ResponseEntity.ok().build();
    }
}
