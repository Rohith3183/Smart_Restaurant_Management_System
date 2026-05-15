package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 1–5 rating each
    private int food;
    private int ambiance;
    private int ingredients;
    private int service;
    private int cleanliness;
    private int valueForMoney;
    private int overall;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
