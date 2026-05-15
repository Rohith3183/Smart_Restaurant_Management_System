package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "employees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    public enum Role {
        OWNER,
        ACCOUNTANT,
        CHEF,
        WAITER,
        INVENTORY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Double salary;

    @Column(nullable = false)
    private boolean active = true;
}
