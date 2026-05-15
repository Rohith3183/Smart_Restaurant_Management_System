package com.smartrestaurant.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.smartrestaurant.backend.dto.FeedbackAnalyticsDto;

public class AnalyticsDto {

    private Long totalOrders;
    private Long totalMenuItems;
    private Long totalEmployees;
    private Long totalTables;

    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal yearRevenue;

    private BigDecimal weekInvestment;
    private BigDecimal monthInvestment;
    private BigDecimal yearInvestment;

    private BigDecimal weekProfit;
    private BigDecimal monthProfit;
    private BigDecimal yearProfit;

    // New advanced analytics fields
    private BigDecimal avgOrderValue;
    private BigDecimal tableTurnover;
    private BigDecimal customerSatisfaction;     // e.g. 4.6
    private Integer avgWaitTimeMinutes;          // e.g. 18

    private List<Map<String, Object>> topDishes;
    private List<Map<String, Object>> profitLossGraph;

    // New lists for charts/cards
    private List<Map<String, Object>> peakHours;           // [{hour, hourLabel, orders, revenue}]
    private List<Map<String, Object>> hourlyOrdersRevenue; // [{hour, hourLabel, orders, revenue}]
    private List<Map<String, Object>> weeklyRevenue;       // [{day, revenue}]
    private List<Map<String, Object>> ordersByCategory;    // [{category, count}]
    private List<Map<String, Object>> topSellingItems;     // [{name, orders, revenue}]
    private List<Map<String, Object>> recentOrders;        // [{id, tableNumber, itemsCount, customerName, status, total}]
    
    // Getters and setters

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalMenuItems() {
        return totalMenuItems;
    }

    public void setTotalMenuItems(Long totalMenuItems) {
        this.totalMenuItems = totalMenuItems;
    }

    public Long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Long getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(Long totalTables) {
        this.totalTables = totalTables;
    }

    public BigDecimal getWeekRevenue() {
        return weekRevenue;
    }

    public void setWeekRevenue(BigDecimal weekRevenue) {
        this.weekRevenue = weekRevenue;
    }

    public BigDecimal getMonthRevenue() {
        return monthRevenue;
    }

    public void setMonthRevenue(BigDecimal monthRevenue) {
        this.monthRevenue = monthRevenue;
    }

    public BigDecimal getYearRevenue() {
        return yearRevenue;
    }

    public void setYearRevenue(BigDecimal yearRevenue) {
        this.yearRevenue = yearRevenue;
    }

    public BigDecimal getWeekInvestment() {
        return weekInvestment;
    }

    public void setWeekInvestment(BigDecimal weekInvestment) {
        this.weekInvestment = weekInvestment;
    }

    public BigDecimal getMonthInvestment() {
        return monthInvestment;
    }

    public void setMonthInvestment(BigDecimal monthInvestment) {
        this.monthInvestment = monthInvestment;
    }

    public BigDecimal getYearInvestment() {
        return yearInvestment;
    }

    public void setYearInvestment(BigDecimal yearInvestment) {
        this.yearInvestment = yearInvestment;
    }

    public BigDecimal getWeekProfit() {
        return weekProfit;
    }

    public void setWeekProfit(BigDecimal weekProfit) {
        this.weekProfit = weekProfit;
    }

    public BigDecimal getMonthProfit() {
        return monthProfit;
    }

    public void setMonthProfit(BigDecimal monthProfit) {
        this.monthProfit = monthProfit;
    }

    public BigDecimal getYearProfit() {
        return yearProfit;
    }

    public void setYearProfit(BigDecimal yearProfit) {
        this.yearProfit = yearProfit;
    }

    public BigDecimal getAvgOrderValue() {
        return avgOrderValue;
    }

    public void setAvgOrderValue(BigDecimal avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }

    public BigDecimal getTableTurnover() {
        return tableTurnover;
    }

    public void setTableTurnover(BigDecimal tableTurnover) {
        this.tableTurnover = tableTurnover;
    }

    public BigDecimal getCustomerSatisfaction() {
        return customerSatisfaction;
    }

    public void setCustomerSatisfaction(BigDecimal customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }

    public Integer getAvgWaitTimeMinutes() {
        return avgWaitTimeMinutes;
    }

    public void setAvgWaitTimeMinutes(Integer avgWaitTimeMinutes) {
        this.avgWaitTimeMinutes = avgWaitTimeMinutes;
    }

    public List<Map<String, Object>> getTopDishes() {
        return topDishes;
    }

    public void setTopDishes(List<Map<String, Object>> topDishes) {
        this.topDishes = topDishes;
    }

    public List<Map<String, Object>> getProfitLossGraph() {
        return profitLossGraph;
    }

    public void setProfitLossGraph(List<Map<String, Object>> profitLossGraph) {
        this.profitLossGraph = profitLossGraph;
    }

    public List<Map<String, Object>> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<Map<String, Object>> peakHours) {
        this.peakHours = peakHours;
    }

    public List<Map<String, Object>> getHourlyOrdersRevenue() {
        return hourlyOrdersRevenue;
    }

    public void setHourlyOrdersRevenue(List<Map<String, Object>> hourlyOrdersRevenue) {
        this.hourlyOrdersRevenue = hourlyOrdersRevenue;
    }

    public List<Map<String, Object>> getWeeklyRevenue() {
        return weeklyRevenue;
    }

    public void setWeeklyRevenue(List<Map<String, Object>> weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue;
    }

    public List<Map<String, Object>> getOrdersByCategory() {
        return ordersByCategory;
    }

    public void setOrdersByCategory(List<Map<String, Object>> ordersByCategory) {
        this.ordersByCategory = ordersByCategory;
    }

    public List<Map<String, Object>> getTopSellingItems() {
        return topSellingItems;
    }

    public void setTopSellingItems(List<Map<String, Object>> topSellingItems) {
        this.topSellingItems = topSellingItems;
    }

    public List<Map<String, Object>> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Map<String, Object>> recentOrders) {
        this.recentOrders = recentOrders;
    }
    
    private FeedbackAnalyticsDto feedback;

    public FeedbackAnalyticsDto getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackAnalyticsDto feedback) {
        this.feedback = feedback;
    }
}
