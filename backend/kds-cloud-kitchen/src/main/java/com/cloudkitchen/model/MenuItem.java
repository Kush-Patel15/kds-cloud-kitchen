package com.cloudkitchen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "prep_time_minutes", nullable = false)
    private Integer prepTimeMinutes = 0;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "is_popular")
    private Boolean isPopular;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "allergen_info")
    private String allergenInfo;

    @Column(name = "nutritional_info", length = 1000)
    private String nutritionalInfo;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitchen_station_id")
    private KitchenStation kitchenStation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (isPopular == null) {
            isPopular = false;
        }
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }
        if (ratingCount == null) {
            ratingCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Category {
        PIZZA, BURGERS, SALADS, WRAPS, SIDES, BEVERAGES, DESSERTS, APPETIZERS, STARTER, DRINK
    }
}