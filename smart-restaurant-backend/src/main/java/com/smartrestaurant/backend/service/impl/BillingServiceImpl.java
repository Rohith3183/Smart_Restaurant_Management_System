package com.smartrestaurant.backend.service.impl;

import com.smartrestaurant.backend.dto.BillDto;
import com.smartrestaurant.backend.entity.Activity;
import com.smartrestaurant.backend.service.ActivityService;
import com.smartrestaurant.backend.dto.CreateBillRequest;
import com.smartrestaurant.backend.entity.*;
import com.smartrestaurant.backend.repository.*;
import com.smartrestaurant.backend.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final BillRepository billRepository;
    private final RestaurantTableRepository tableRepository;
    private final ActivityService activityService;
    private final CustomerRepository customerRepository;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.18); // 18% GST

    public BillingServiceImpl(RestaurantRepository restaurantRepository,
                             OrderRepository orderRepository,
                             BillRepository billRepository,
                             RestaurantTableRepository tableRepository,
                             ActivityService activityService,
                             CustomerRepository customerRepository) {
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
        this.tableRepository = tableRepository;
        this.activityService = activityService;
        this.customerRepository = customerRepository;
    }

    @Override
    public BillDto createBill(String restaurantCode, CreateBillRequest request) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }

        if (order.getStatus() != OrderEntity.Status.SERVED) {
            throw new IllegalArgumentException("Order must be SERVED before billing");
        }
        
        // ADD THIS: Save customer phone if provided
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
            String phone = request.getCustomerPhone().trim();
            // Check if customer already exists
            if (customerRepository.findByRestaurantAndPhone(restaurant, phone).isEmpty()) {
                // Customer doesn't exist, save new customer
                Customer customer = Customer.builder()
                        .restaurant(restaurant)
                        .phone(phone)
                        .createdAt(LocalDateTime.now())
                        .build();
                customerRepository.save(customer);
            }
            // If customer exists, skip (proceed normally)
        }

        BigDecimal total = order.getTotalAmount();
        BigDecimal tax = total.multiply(TAX_RATE);
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = total.add(tax).subtract(discount);

        Bill bill = Bill.builder()
                .order(order)
                .total(total)
                .tax(tax)
                .discount(discount)
                .finalAmount(finalAmount)
                .paymentMethod(Bill.PaymentMethod.valueOf(request.getPaymentMethod()))
                .createdAt(LocalDateTime.now())
                .build();

        Bill saved = billRepository.save(bill);
        
        activityService.log(
        	    restaurantCode,
        	    Activity.Type.BILL_GENERATED,
        	    String.format("Bill generated for Order #%d, amount ₹%s, paid via %s",
        	                  order.getId(),
        	                  saved.getFinalAmount().toPlainString(),
        	                  saved.getPaymentMethod().name()),
        	    "ACCOUNTANT"
        	);

        // Mark order as CLOSED
        order.setStatus(OrderEntity.Status.CLOSED);
        orderRepository.save(order);

        // Free the table if dine-in
        if (order.getTable() != null) {
            RestaurantTable table = order.getTable();
            table.setStatus(RestaurantTable.Status.FREE);
            tableRepository.save(table);
        }

        return toDto(saved);
    }

    @Override
    public BillDto getBillByOrderId(String restaurantCode, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Order does not belong to this restaurant");
        }

        Bill bill = billRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found for this order"));

        return toDto(bill);
    }
    
    @Override
    public List<BillDto> getAllBills(String restaurantCode) {
        Restaurant restaurant = restaurantRepository.findByCode(restaurantCode)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return billRepository.findByOrder_RestaurantOrderByCreatedAtDesc(restaurant)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private BillDto toDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setOrderId(bill.getOrder().getId());
        dto.setTotal(bill.getTotal());
        dto.setTax(bill.getTax());
        dto.setDiscount(bill.getDiscount());
        dto.setFinalAmount(bill.getFinalAmount());
        dto.setPaymentMethod(bill.getPaymentMethod().name());
        dto.setCreatedAt(bill.getCreatedAt());
        return dto;
    }
}
