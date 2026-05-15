package com.smartrestaurant.backend.service;

import com.smartrestaurant.backend.dto.BillDto;
import com.smartrestaurant.backend.dto.CreateBillRequest;

import java.util.List;

public interface BillingService {
    BillDto createBill(String restaurantCode, CreateBillRequest request);
    BillDto getBillByOrderId(String restaurantCode, Long orderId);
    List<BillDto> getAllBills(String restaurantCode);
}
