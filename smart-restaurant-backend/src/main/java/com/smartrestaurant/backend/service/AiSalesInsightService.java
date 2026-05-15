package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.AiInsightDto;
import com.smartrestaurant.backend.dto.AnalyticsDto;
import org.springframework.stereotype.Service;

@Service
public class AiSalesInsightService {

    private final AnalyticsService analyticsService;
    private final AiModelClient aiModelClient;

    public AiSalesInsightService(AnalyticsService analyticsService,
                                 AiModelClient aiModelClient) {
        this.analyticsService = analyticsService;
        this.aiModelClient = aiModelClient;
    }

    public AiInsightDto generateInsights(String restaurantCode) {
        AnalyticsDto a = analyticsService.getAnalytics(restaurantCode);

        String prompt = """
                You are an AI assistant for a single specific restaurant.
                You MUST use ONLY the numeric values and items from the data below.
                Do NOT invent any numbers, dates, menu items, or locations that are not in the data.

                DATA (JSON-like):

                totalOrders: %d
                totalMenuItems: %d
                totalEmployees: %d
                totalTables: %d

                weekRevenue: %s
                monthRevenue: %s
                yearRevenue: %s

                weekInvestment: %s
                monthInvestment: %s
                yearInvestment: %s

                weekProfit: %s
                monthProfit: %s
                yearProfit: %s

                avgOrderValue: %s
                tableTurnover: %s
                customerSatisfaction: %s
                avgWaitTimeMinutes: %s

                peakHours: %s
                hourlyOrdersRevenue: %s
                weeklyRevenue: %s
                ordersByCategory: %s
                topSellingItems: %s
                recentOrders: %s

                TASK:

                1. Write a short paragraph summary of performance for the OWNER.
                2. Give 5–7 bullet points of key insights (refer to exact metrics).
                3. Give 3–5 concrete recommendations tied to the data.

                RULES:
                - Do not mention any other restaurants or generic examples.
                - Always mention at least one top selling item by name if available.
                - If some lists are empty, say clearly that there is not enough data.
                - All monetary values are in Indian Rupees. Use the ₹ symbol, NOT $, when you mention money.
                - Output in markdown with headings: ## Summary, ## Key Insights, ## Recommendations.
                """
                .formatted(
                        a.getTotalOrders(),
                        a.getTotalMenuItems(),
                        a.getTotalEmployees(),
                        a.getTotalTables(),
                        a.getWeekRevenue(),
                        a.getMonthRevenue(),
                        a.getYearRevenue(),
                        a.getWeekInvestment(),
                        a.getMonthInvestment(),
                        a.getYearInvestment(),
                        a.getWeekProfit(),
                        a.getMonthProfit(),
                        a.getYearProfit(),
                        a.getAvgOrderValue(),
                        a.getTableTurnover(),
                        a.getCustomerSatisfaction(),
                        a.getAvgWaitTimeMinutes(),
                        a.getPeakHours(),
                        a.getHourlyOrdersRevenue(),
                        a.getWeeklyRevenue(),
                        a.getOrdersByCategory(),
                        a.getTopSellingItems(),
                        a.getRecentOrders()
                );

        String reply = aiModelClient.generateInsights(prompt);
        return new AiInsightDto(reply);
    }
}
