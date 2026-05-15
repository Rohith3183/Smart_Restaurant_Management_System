package com.smartrestaurant.backend.repository;

import com.smartrestaurant.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

//PurchaseOrderItemRepository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
 List<PurchaseOrderItem> findByPurchaseOrder(PurchaseOrder purchaseOrder);
}