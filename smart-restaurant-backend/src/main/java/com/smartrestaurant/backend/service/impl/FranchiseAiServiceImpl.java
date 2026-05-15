package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.AiInsightDto;
import com.smartrestaurant.backend.dto.AnalyticsDto;
import com.smartrestaurant.backend.dto.ChatMessageDto;
import com.smartrestaurant.backend.dto.ChatRequestDto;
import com.smartrestaurant.backend.dto.ChatResponseDto;
import com.smartrestaurant.backend.entity.Restaurant;
import com.smartrestaurant.backend.repository.RestaurantRepository;
import com.smartrestaurant.backend.service.AiModelClient;
import com.smartrestaurant.backend.service.AnalyticsService;
import com.smartrestaurant.backend.service.FranchiseAiService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FranchiseAiServiceImpl implements FranchiseAiService {

    private final RestaurantRepository restaurantRepository;
    private final AnalyticsService analyticsService;
    private final AiModelClient aiModelClient;

    public FranchiseAiServiceImpl(RestaurantRepository restaurantRepository,
                                  AnalyticsService analyticsService,
                                  AiModelClient aiModelClient) {
        this.restaurantRepository = restaurantRepository;
        this.analyticsService = analyticsService;
        this.aiModelClient = aiModelClient;
    }

    @Override
    public AiInsightDto generateInsights(String franchiseCode) {
        String code = franchiseCode.toUpperCase().trim();
        List<Restaurant> restaurants = restaurantRepository.findByFranchise_Code(code);

        if (restaurants.isEmpty()) {
            return new AiInsightDto("No restaurants found under this franchise yet.");
        }

        // 1) Aggregate analytics across all restaurants (similar to FranchiseAnalyticsServiceImpl)
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

        BigDecimal avgOrderValueSum = BigDecimal.ZERO;
        int avgOrderValueCount = 0;

        BigDecimal tableTurnoverSum = BigDecimal.ZERO;
        int tableTurnoverCount = 0;

        BigDecimal customerSatisfactionSum = BigDecimal.ZERO;
        int satisfactionCount = 0;

        BigDecimal avgWaitTimeSum = BigDecimal.ZERO;
        int avgWaitTimeCount = 0;

        // We'll also build a short per-restaurant line for context
        StringBuilder perRestaurantSummary = new StringBuilder();
        perRestaurantSummary.append("[\n");

        for (Restaurant r : restaurants) {
            AnalyticsDto a = analyticsService.getAnalytics(r.getCode());

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

            if (a.getAvgOrderValue() != null) {
                avgOrderValueSum = avgOrderValueSum.add(a.getAvgOrderValue());
                avgOrderValueCount++;
            }
            if (a.getTableTurnover() != null) {
                tableTurnoverSum = tableTurnoverSum.add(a.getTableTurnover());
                tableTurnoverCount++;
            }
            if (a.getCustomerSatisfaction() != null) {
                customerSatisfactionSum = customerSatisfactionSum.add(a.getCustomerSatisfaction());
                satisfactionCount++;
            }
            if (a.getAvgWaitTimeMinutes() != null) {
                avgWaitTimeSum = avgWaitTimeSum.add(
                        BigDecimal.valueOf(a.getAvgWaitTimeMinutes())
                );
                avgWaitTimeCount++;
            }

            perRestaurantSummary.append(String.format(
                    "  { name: \"%s\", code: \"%s\", yearRevenue: %s, customerSatisfaction: %s },\n",
                    r.getName(),
                    r.getCode(),
                    a.getYearRevenue(),
                    a.getCustomerSatisfaction()
            ));
        }
        perRestaurantSummary.append("]");

        BigDecimal avgOrderValue =
                avgOrderValueCount > 0
                        ? avgOrderValueSum.divide(BigDecimal.valueOf(avgOrderValueCount))
                        : BigDecimal.ZERO;

        BigDecimal tableTurnover =
                tableTurnoverCount > 0
                        ? tableTurnoverSum.divide(BigDecimal.valueOf(tableTurnoverCount))
                        : BigDecimal.ZERO;

        BigDecimal customerSatisfaction =
                satisfactionCount > 0
                        ? customerSatisfactionSum.divide(BigDecimal.valueOf(satisfactionCount))
                        : BigDecimal.ZERO;

        BigDecimal avgWaitTime =
                avgWaitTimeCount > 0
                        ? avgWaitTimeSum.divide(BigDecimal.valueOf(avgWaitTimeCount))
                        : BigDecimal.ZERO;

        // 2) Build prompt similar to AiSalesInsightService but for FRANCHISE
        String prompt = """
                You are an AI assistant for a single restaurant FRANCHISE (group of restaurants).
                You MUST use ONLY the numeric values and items from the data below.
                Do NOT invent any numbers, dates, menu items, or locations that are not in the data.

                FRANCHISE CODE: %s
                NUMBER OF RESTAURANTS: %d

                COMBINED DATA (JSON-like):

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

                avgOrderValue (average across restaurants): %s
                tableTurnover (average across restaurants): %s
                customerSatisfaction (average across restaurants): %s
                avgWaitTimeMinutes (average across restaurants): %s

                perRestaurantSummary: %s

                TASK:

                1. Write a short paragraph summary of performance for the FRANCHISE OWNER.
                2. Give 5–8 bullet points of key insights across the franchise (mention restaurant codes/names where relevant).
                3. Give 4–6 concrete recommendations for the franchise level and also mention at least 2 specific restaurants in examples.

                RULES:
                - Always talk about the franchise as a group, but you may highlight individual restaurants.
                - Do not invent specific menu items; if you mention "top items", keep it generic unless present in the data.
                - If some metrics look empty or zero, say that data is limited.
                - All monetary values are in Indian Rupees. Use the ₹ symbol, NOT $, when you mention money.
                - Output in markdown with headings: ## Summary, ## Key Insights, ## Recommendations.
                """
                .formatted(
                        code,
                        restaurants.size(),
                        totalOrders,
                        totalMenuItems,
                        totalEmployees,
                        totalTables,
                        weekRevenue,
                        monthRevenue,
                        yearRevenue,
                        weekInvestment,
                        monthInvestment,
                        yearInvestment,
                        weekProfit,
                        monthProfit,
                        yearProfit,
                        avgOrderValue,
                        tableTurnover,
                        customerSatisfaction,
                        avgWaitTime,
                        perRestaurantSummary
                );

        String reply = aiModelClient.generateInsights(prompt);
        return new AiInsightDto(reply);
    }

    @Override
    public ChatResponseDto chat(String franchiseCode, ChatRequestDto req) {
        String code = franchiseCode.toUpperCase().trim();
        List<Restaurant> restaurants = restaurantRepository.findByFranchise_Code(code);

        // To keep behaviour consistent with AiChatService, we create a strong system message
        // but for FRANCHISE instead of single restaurant.
        StringBuilder baseContext = new StringBuilder();
        baseContext.append("You are an AI assistant for a restaurant FRANCHISE owner, not a single outlet.\n");
        baseContext.append("Franchise code: ").append(code).append("\n");
        baseContext.append("Restaurants under this franchise:\n");

        for (Restaurant r : restaurants) {
            baseContext.append("- ")
                    .append(r.getName())
                    .append(" (")
                    .append(r.getCode())
                    .append(")\n");
        }

        baseContext.append("\nRULES:\n")
                .append("- Answer on behalf of the FRANCHISE owner.\n")
                .append("- When you mention performance, talk about the franchise or specific restaurant codes.\n")
                .append("- If you need numbers, ask the owner to check the analytics tab if not provided in context.\n")
                .append("- Keep answers in 3–6 short sentences, practical and clear.\n")
                .append("- All monetary values are in Indian Rupees. Use the ₹ symbol, NOT $.\n");

        List<Map<String, String>> messages = new ArrayList<>();

        // System message first
        messages.add(Map.of(
                "role", "system",
                "content", baseContext.toString()
        ));

        // Then user chat history
        if (req.getMessages() != null) {
            for (ChatMessageDto m : req.getMessages()) {
                messages.add(Map.of(
                        "role", m.getRole(),
                        "content", m.getContent()
                ));
            }
        }

        String reply = aiModelClient.chat(messages);
        return new ChatResponseDto(reply);
    }
}
