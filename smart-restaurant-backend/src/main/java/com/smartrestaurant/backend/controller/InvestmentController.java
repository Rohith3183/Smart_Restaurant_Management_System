package com.smartrestaurant.backend.controller;

import com.smartrestaurant.backend.dto.CreateInvestmentRequest;
import com.smartrestaurant.backend.dto.InvestmentDto;
import com.smartrestaurant.backend.service.InvestmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
@CrossOrigin(origins = "*")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ACCOUNTANT')")
    public ResponseEntity<List<InvestmentDto>> getAll(HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(investmentService.getAllInvestments(restaurantCode));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ACCOUNTANT')")
    public ResponseEntity<InvestmentDto> create(@Valid @RequestBody CreateInvestmentRequest req,
                                                 HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        return ResponseEntity.ok(investmentService.createInvestment(restaurantCode, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ACCOUNTANT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String restaurantCode = (String) request.getAttribute("restaurantCode");
        investmentService.deleteInvestment(restaurantCode, id);
        return ResponseEntity.noContent().build();
    }
}
