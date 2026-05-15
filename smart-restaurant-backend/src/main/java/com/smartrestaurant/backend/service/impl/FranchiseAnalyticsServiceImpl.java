package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.dto.LoginResponse;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.entity.User;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.UserRepository;
import com.smartrestaurant.backend.security.jwt.JwtTokenProvider;
import com.smartrestaurant.backend.service.AnalyticsService;
import com.smartrestaurant.backend.service.FranchiseAnalyticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FranchiseAnalyticsServiceImpl implements FranchiseAnalyticsService {

    private final RestaurantRepository restaurantRepository;
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public FranchiseAnalyticsServiceImpl(RestaurantRepository restaurantRepository,
                                         AnalyticsService analyticsService,
                                         UserRepository userRepository,
                                         JwtTokenProvider jwtTokenProvider) {
        this.restaurantRepository = restaurantRepository;
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AnalyticsDto getCombinedAnalytics(String franchiseCode) {
        String code = franchiseCode.toUpperCase().trim();
        List<Restaurant> restaurants = restaurantRepository.findByFranchise_Code(code);

        if (restaurants.isEmpty()) {
            // No restaurants yet – return empty AnalyticsDto
            return new AnalyticsDto();
        }

        AnalyticsDto combined = new AnalyticsDto();

        long totalOrders = 0L;
        long totalMenuItems = 0L;
        long totalEmployees = 0L;
        long totalTables = 0L;

        BigDecimal weekRevenue = BigDecimal.ZERO;
        BigDecimal monthRevenue = BigDecimal.ZERO;
        BigDecimal yearRevenue = BigDecimal.ZERO;

        BigDecimal weekInvestment = BigDecimal.ZERO;
        BigDecimal monthInvestment = BigDecimal.ZERO;
        BigDecimal yearInvestment = BigDecimal.ZERO;

        BigDecimal weekProfit = BigDecimal.ZERO;
        BigDecimal monthProfit = BigDecimal.ZERO;
        BigDecimal yearProfit = BigDecimal.ZERO;

        BigDecimal customerSatisfaction = BigDecimal.ZERO;
        int satisfactionCount = 0;

        for (Restaurant r : restaurants) {
            String restaurantCode = r.getCode();
            AnalyticsDto a = analyticsService.getAnalytics(restaurantCode);

            if (a.getTotalOrders() != null) {
                totalOrders += a.getTotalOrders();
            }
            if (a.getTotalMenuItems() != null) {
                totalMenuItems += a.getTotalMenuItems();
            }
            if (a.getTotalEmployees() != null) {
                totalEmployees += a.getTotalEmployees();
            }
            if (a.getTotalTables() != null) {
                totalTables += a.getTotalTables();
            }

            if (a.getWeekRevenue() != null) {
                weekRevenue = weekRevenue.add(a.getWeekRevenue());
            }
            if (a.getMonthRevenue() != null) {
                monthRevenue = monthRevenue.add(a.getMonthRevenue());
            }
            if (a.getYearRevenue() != null) {
                yearRevenue = yearRevenue.add(a.getYearRevenue());
            }

            if (a.getWeekInvestment() != null) {
                weekInvestment = weekInvestment.add(a.getWeekInvestment());
            }
            if (a.getMonthInvestment() != null) {
                monthInvestment = monthInvestment.add(a.getMonthInvestment());
            }
            if (a.getYearInvestment() != null) {
                yearInvestment = yearInvestment.add(a.getYearInvestment());
            }

            if (a.getWeekProfit() != null) {
                weekProfit = weekProfit.add(a.getWeekProfit());
            }
            if (a.getMonthProfit() != null) {
                monthProfit = monthProfit.add(a.getMonthProfit());
            }
            if (a.getYearProfit() != null) {
                yearProfit = yearProfit.add(a.getYearProfit());
            }

            if (a.getCustomerSatisfaction() != null) {
                customerSatisfaction = customerSatisfaction.add(a.getCustomerSatisfaction());
                satisfactionCount++;
            }

            // For advanced fields like lists (topDishes, peakHours, etc.),
            // you can later merge them if needed.
        }

        combined.setTotalOrders(totalOrders);
        combined.setTotalMenuItems(totalMenuItems);
        combined.setTotalEmployees(totalEmployees);
        combined.setTotalTables(totalTables);

        combined.setWeekRevenue(weekRevenue);
        combined.setMonthRevenue(monthRevenue);
        combined.setYearRevenue(yearRevenue);

        combined.setWeekInvestment(weekInvestment);
        combined.setMonthInvestment(monthInvestment);
        combined.setYearInvestment(yearInvestment);

        combined.setWeekProfit(weekProfit);
        combined.setMonthProfit(monthProfit);
        combined.setYearProfit(yearProfit);

        if (satisfactionCount > 0) {
            combined.setCustomerSatisfaction(
                    customerSatisfaction.divide(BigDecimal.valueOf(satisfactionCount))
            );
        }

        return combined;
    }

    @Override
    public LoginResponse impersonateRestaurant(String franchiseCode, String restaurantCode) {
        String fCode = franchiseCode.toUpperCase().trim();
        String rCode = restaurantCode.toUpperCase().trim();

        Restaurant restaurant = restaurantRepository.findByCode(rCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + rCode));

        if (restaurant.getFranchise() == null ||
                !restaurant.getFranchise().getCode().equalsIgnoreCase(fCode)) {
            throw new IllegalArgumentException("Restaurant does not belong to this franchise");
        }

        // Pick an OWNER user for this restaurant
        User owner = userRepository
                .findByRestaurant_IdAndRole(restaurant.getId(), User.Role.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("Owner user not found for restaurant"));

        String token = jwtTokenProvider.createToken(
                owner.getUsername(),
                owner.getRole().name(),
                restaurant.getCode()
        );

        return new LoginResponse(
                token,
                owner.getUsername(),
                owner.getRole().name(),
                restaurant.getCode(),
                restaurant.getName()
        );
    }
}
