package com.smartrestaurant.backend.entity;

import jakarta.persistence.*;

import lombok.*;

import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String location;

    private String contact;

    @Column(name = "table_count")
    private Integer tableCount;

    private Integer capacity;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id")
    private Franchise franchise;

    // Add getter and setter
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Franchise getFranchise() {
        return franchise;
    }
    public void setFranchise(Franchise franchise) {
        this.franchise = franchise;
    }

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantTable> tables;
}
