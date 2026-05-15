package com.smartrestaurant.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.entity.MenuItem;
import com.smartrestaurant.backend.entity.OrderEntity;
import com.smartrestaurant.backend.entity.OrderItem;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.EmployeeRepository;
import com.smartrestaurant.backend.repository.InvestmentRepository;
import com.smartrestaurant.backend.repository.MenuItemRepository;
import com.smartrestaurant.backend.repository.OrderItemRepository;
import com.smartrestaurant.backend.repository.OrderRepository;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.repository.RestaurantTableRepository;
import com.smartrestaurant.backend.service.AnalyticsService;
import com.smartrestaurant.backend.service.FeedbackService;

@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final EmployeeRepository employeeRepository;
    private final RestaurantTableRepository tableRepository;
    private final InvestmentRepository investmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final FeedbackService feedbackService;

    public AnalyticsServiceImpl(RestaurantRepository restaurantRepository,
            OrderRepository orderRepository,
            MenuItemRepository menuItemRepository,
            EmployeeRepository employeeRepository,
            RestaurantTableRepository tableRepository,
            InvestmentRepository investmentRepository,
            OrderItemRepository orderItemRepository,
            FeedbackService feedbackService) {
this.restaurantRepository = restaurantRepository;
this.orderRepository = orderRepository;
this.menuItemRepository = menuItemRepository;
this.employeeRepository = employeeRepository;
this.tableRepository = tableRepository;
this.investmentRepository = investmentRepository;
this.orderItemRepository = orderItemRepository;
this.feedbackService = feedbackService;
}

    @Override
    public AnalyticsDto getAnalytics(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        AnalyticsDto analytics = new AnalyticsDto();

        // Basic counts
        analytics.setTotalOrders(orderRepository.getTotalCompletedOrders(restaurant));
        analytics.setTotalMenuItems((long) menuItemRepository.findByRestaurant(restaurant).size());
        analytics.setTotalEmployees((long) employeeRepository.findByRestaurant(restaurant).size());
        analytics.setTotalTables((long) tableRepository.findByRestaurant(restaurant).size());

        // Time periods
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);
        LocalDateTime monthAgo = now.minusMonths(1);
        LocalDateTime yearAgo = now.minusYears(1);

        // Revenue calculations
        Double weekRev = orderRepository.getTotalRevenueSince(restaurant, weekAgo);
        Double monthRev = orderRepository.getTotalRevenueSince(restaurant, monthAgo);
        Double yearRev = orderRepository.getTotalRevenueSince(restaurant, yearAgo);

        analytics.setWeekRevenue(weekRev != null ? BigDecimal.valueOf(weekRev) : BigDecimal.ZERO);
        analytics.setMonthRevenue(monthRev != null ? BigDecimal.valueOf(monthRev) : BigDecimal.ZERO);
        analytics.setYearRevenue(yearRev != null ? BigDecimal.valueOf(yearRev) : BigDecimal.ZERO);

        // Investment calculations
        Double weekInv = investmentRepository.getTotalNonSalaryInvestmentSince(restaurant, weekAgo); // exclude salary
        Double monthInv = investmentRepository.getTotalInvestmentSince(restaurant, monthAgo);        // include all
        Double yearInv = investmentRepository.getTotalInvestmentSince(restaurant, yearAgo);          // include all

        analytics.setWeekInvestment(weekInv != null ? BigDecimal.valueOf(weekInv) : BigDecimal.ZERO);
        analytics.setMonthInvestment(monthInv != null ? BigDecimal.valueOf(monthInv) : BigDecimal.ZERO);
        analytics.setYearInvestment(yearInv != null ? BigDecimal.valueOf(yearInv) : BigDecimal.ZERO);

        // Profit/Loss = Revenue - Investment
        analytics.setWeekProfit(analytics.getWeekRevenue().subtract(analytics.getWeekInvestment()));
        analytics.setMonthProfit(analytics.getMonthRevenue().subtract(analytics.getMonthInvestment()));
        analytics.setYearProfit(analytics.getYearRevenue().subtract(analytics.getYearInvestment()));

        // ---------------- ADVANCED METRICS (using CLOSED orders only) ----------------
        List<OrderEntity> closedOrders =
                orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.CLOSED);

        // Avg order value
        BigDecimal totalClosedRevenue = closedOrders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!closedOrders.isEmpty()) {
            analytics.setAvgOrderValue(
                    totalClosedRevenue.divide(
                            BigDecimal.valueOf(closedOrders.size()),
                            2,
                            BigDecimal.ROUND_HALF_UP
                    )
            );
        } else {
            analytics.setAvgOrderValue(BigDecimal.ZERO);
        }

        // Table turnover = closed orders / total tables
        if (analytics.getTotalTables() != null && analytics.getTotalTables() > 0) {
            BigDecimal tableTurnover = BigDecimal
                    .valueOf(closedOrders.size())
                    .divide(BigDecimal.valueOf(analytics.getTotalTables()), 2, BigDecimal.ROUND_HALF_UP);
            analytics.setTableTurnover(tableTurnover);
        } else {
            analytics.setTableTurnover(BigDecimal.ZERO);
        }

        // Placeholder customer satisfaction & wait time
        analytics.setCustomerSatisfaction(BigDecimal.valueOf(4.6));
        analytics.setAvgWaitTimeMinutes(18);

        // Peak hours + hourly orders/revenue (approximate using CLOSED orders)
        List<Map<String, Object>> hourly = buildHourlyFromClosedOrders(closedOrders);
        analytics.setHourlyOrdersRevenue(hourly);
        analytics.setPeakHours(extractTopPeakHours(hourly));

        // Weekly revenue (approximate from CLOSED orders, last 7 days)
        analytics.setWeeklyRevenue(buildWeeklyFromClosedOrders(closedOrders, now));

        // Orders by category + top selling items
        analytics.setOrdersByCategory(buildOrdersByCategory(closedOrders));
        analytics.setTopSellingItems(buildTopSellingItems(closedOrders));

        // Recent orders (latest CLOSED 5)
        analytics.setRecentOrders(buildRecentOrders(closedOrders));

        // Existing charts
        analytics.setTopDishes(getTopDishes(restaurant));
        analytics.setProfitLossGraph(getProfitLossGraph(restaurant));
        analytics.setFeedback(feedbackService.getFeedbackAnalytics(restaurantCode));
        
        return analytics;
    }

    // ---------------- EXISTING METHODS ----------------

    private List<Map<String, Object>> getTopDishes(Restaurant restaurant) {
        List<OrderEntity> closedOrders = orderRepository.findByRestaurantAndStatus(restaurant, OrderEntity.Status.CLOSED);

        Map<String, Integer> dishCount = new HashMap<>();
        Map<String, String> dishCategory = new HashMap<>();

        for (OrderEntity order : closedOrders) {
            for (OrderItem item : order.getItems()) {
                String dishName = item.getMenuItem().getName();
                String category = item.getMenuItem().getCategory().getName();
                dishCount.put(dishName, dishCount.getOrDefault(dishName, 0) + item.getQuantity());
                dishCategory.put(dishName, category);
            }
        }

        return dishCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("count", entry.getValue());
                    map.put("category", dishCategory.get(entry.getKey()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getProfitLossGraph(Restaurant restaurant) {
        List<Map<String, Object>> graph = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now()
                    .minusMonths(i)
                    .withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);

            Double revenue = orderRepository.getTotalRevenueSince(restaurant, monthStart);
            if (revenue == null) revenue = 0.0;

            Double investment = investmentRepository.getTotalInvestmentSince(restaurant, monthStart);
            if (investment == null) investment = 0.0;

            LocalDateTime prevMonthStart = monthStart.minusMonths(1);
            Double prevRevenue = orderRepository.getTotalRevenueSince(restaurant, prevMonthStart);
            Double prevInvestment = investmentRepository.getTotalInvestmentSince(restaurant, prevMonthStart);

            revenue = revenue - (prevRevenue != null ? prevRevenue : 0.0);
            investment = investment - (prevInvestment != null ? prevInvestment : 0.0);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("month", monthStart.format(formatter));
            dataPoint.put("revenue", revenue);
            dataPoint.put("investment", investment);
            dataPoint.put("profit", revenue - investment);

            graph.add(dataPoint);
        }

        return graph;
    }

    // ---------------- NEW HELPERS (no new repository methods) ----------------

    private List<Map<String, Object>> buildHourlyFromClosedOrders(List<OrderEntity> closedOrders) {
        Map<Integer, Map<String, Object>> byHour = new HashMap<>();

        for (OrderEntity order : closedOrders) {
            if (order.getCreatedAt() == null) continue;
            int hour = order.getCreatedAt().getHour();

            Map<String, Object> bucket = byHour.computeIfAbsent(hour, h -> {
                Map<String, Object> m = new HashMap<>();
                m.put("hour", h);
                m.put("hourLabel", String.format("%02d:00", h));
                m.put("orders", 0L);
                m.put("revenue", BigDecimal.ZERO);
                return m;
            });

            long currentCount = (Long) bucket.get("orders");
            bucket.put("orders", currentCount + 1);

            BigDecimal currentRev = (BigDecimal) bucket.get("revenue");
            BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            bucket.put("revenue", currentRev.add(orderTotal));
        }

        return byHour.values().stream()
                .sorted(Comparator.comparingInt(m -> (Integer) m.get("hour")))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> extractTopPeakHours(List<Map<String, Object>> hourly) {
        if (hourly == null) return Collections.emptyList();
        return hourly.stream()
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("orders"),
                        (Long) a.get("orders")))
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildWeeklyFromClosedOrders(List<OrderEntity> closedOrders, LocalDateTime now) {
        Map<String, BigDecimal> byDay = new LinkedHashMap<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE");

        for (int i = 6; i >= 0; i--) {
            String label = now.minusDays(i).format(dayFmt);
            byDay.put(label, BigDecimal.ZERO);
        }

        for (OrderEntity order : closedOrders) {
            if (order.getCreatedAt() == null) continue;
            String label = order.getCreatedAt().format(dayFmt);
            if (!byDay.containsKey(label)) continue; // older than 7 days
            BigDecimal current = byDay.get(label);
            BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            byDay.put(label, current.add(total));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : byDay.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("day", e.getKey());
            m.put("revenue", e.getValue());
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> buildOrdersByCategory(List<OrderEntity> closedOrders) {
        Map<String, Long> counts = new HashMap<>();

        for (OrderEntity order : closedOrders) {
            for (OrderItem item : order.getItems()) {
                MenuItem menuItem = item.getMenuItem();
                if (menuItem == null || menuItem.getCategory() == null) continue;
                String cat = menuItem.getCategory().getName();
                counts.put(cat, counts.getOrDefault(cat, 0L) + item.getQuantity());
            }
        }

        return counts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("category", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTopSellingItems(List<OrderEntity> closedOrders) {
        Map<String, Long> countMap = new HashMap<>();
        Map<String, BigDecimal> revenueMap = new HashMap<>();

        for (OrderEntity order : closedOrders) {
            for (OrderItem item : order.getItems()) {
                MenuItem menuItem = item.getMenuItem();
                if (menuItem == null) continue;
                String name = menuItem.getName();
                BigDecimal price = menuItem.getPrice() != null ? menuItem.getPrice() : BigDecimal.ZERO;
                long qty = item.getQuantity();

                countMap.put(name, countMap.getOrDefault(name, 0L) + qty);
                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
                revenueMap.put(name, revenueMap.getOrDefault(name, BigDecimal.ZERO).add(lineTotal));
            }
        }

        return countMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("orders", e.getValue());
                    m.put("revenue", revenueMap.getOrDefault(e.getKey(), BigDecimal.ZERO));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildRecentOrders(List<OrderEntity> closedOrders) {
        return closedOrders.stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(order -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", order.getId());
                    m.put("tableNumber",
                            order.getTable() != null ? order.getTable().getTableNumber() : null);
                    m.put("itemsCount",
                            order.getItems() != null
                                    ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum()
                                    : 0);
                    m.put("customerName", null); // placeholder for future
                    m.put("status", order.getStatus().name());
                    m.put("total",
                            order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                    return m;
                })
                .collect(Collectors.toList());
    }
}
