package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.CreateInvestmentRequest;
import com.smartrestaurant.backend.dto.InvestmentDto;

import java.util.List;

public interface InvestmentService {
    InvestmentDto createInvestment(String restaurantCode, CreateInvestmentRequest request);
    List<InvestmentDto> getAllInvestments(String restaurantCode);
    void deleteInvestment(String restaurantCode, Long id);
}
