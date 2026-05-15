package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    public enum Type {
        MENU_CREATED,
        MENU_UPDATED,
        INGREDIENT_CREATED,
        INGREDIENT_DELETED,
        STOCK_UPDATED,
        EMPLOYEE_CREATED,
        EMPLOYEE_DELETED,
        INVESTMENT_CREATED,
        SALARY_PAID,
        ORDER_CREATED,
        ORDER_STATUS_CHANGED,
        BILL_GENERATED,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        LOW_STOCK_ALERT,
        CONTACT_CREATED,
        CONTACT_UPDATED,
        CONTACT_DELETED,
        CONTACT_MESSAGE_SENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Type type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "actor_name", length = 100)
    private String actorName; // e.g., "Chef Arjun"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
