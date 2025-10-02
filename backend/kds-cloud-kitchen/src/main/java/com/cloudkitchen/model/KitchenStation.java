package com.cloudkitchen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kitchen_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationType type;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "max_concurrent_orders")
    private Integer maxConcurrentOrders;

    @Column(name = "average_prep_time_minutes")
    private Integer averagePrepTimeMinutes;

    @OneToMany(mappedBy = "kitchenStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "assignedStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> assignedOrders;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (maxConcurrentOrders == null) {
            maxConcurrentOrders = 10;
        }
        if (averagePrepTimeMinutes == null) {
            averagePrepTimeMinutes = 15;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum StationType {
        GRILL, FRYER, ASSEMBLY, SALAD, PIZZA, BEVERAGE, EXPEDITE, GENERAL
    }
}