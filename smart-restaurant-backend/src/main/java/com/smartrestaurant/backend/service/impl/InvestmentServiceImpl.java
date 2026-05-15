package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.CreateInvestmentRequest;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.dto.InvestmentDto;
import com.smartrestaurant.backend.entity.Investment;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.InvestmentRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.InvestmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvestmentServiceImpl implements InvestmentService {

    private final RestaurantRepository restaurantRepository;
    private final InvestmentRepository investmentRepository;
    private final ActivityService activityService;

    public InvestmentServiceImpl(RestaurantRepository restaurantRepository,
                                InvestmentRepository investmentRepository,
                                ActivityService activityService) {
        this.restaurantRepository = restaurantRepository;
        this.investmentRepository = investmentRepository;
        this.activityService = activityService;
    }

    @Override
    public InvestmentDto createInvestment(String restaurantCode, CreateInvestmentRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Investment investment = Investment.builder()
                .restaurant(restaurant)
                .amount(request.getAmount())
                .description(request.getDescription())
                .type(Investment.Type.valueOf(request.getType()))
                .createdAt(LocalDateTime.now())
                .build();

        Investment saved = investmentRepository.save(investment);
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.INVESTMENT_CREATED,
        	    String.format("Investment recorded: ₹%s – %s (%s)",
        	                  saved.getAmount().toPlainString(),
        	                  saved.getDescription(),
        	                  saved.getType().name()),
        	    "OWNER"
        	);
        return toDto(saved);
    }

    @Override
    public List<InvestmentDto> getAllInvestments(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return investmentRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteInvestment(String restaurantCode, Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found"));

        if (!investment.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Investment does not belong to this restaurant");
        }

        investmentRepository.delete(investment);
    }

    private InvestmentDto toDto(Investment investment) {
        InvestmentDto dto = new InvestmentDto();
        dto.setId(investment.getId());
        dto.setAmount(investment.getAmount());
        dto.setDescription(investment.getDescription());
        dto.setType(investment.getType().name());
        dto.setCreatedAt(investment.getCreatedAt());
        return dto;
    }
}
