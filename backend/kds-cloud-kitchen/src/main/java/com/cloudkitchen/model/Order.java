package com.cloudkitchen.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id", unique = true, nullable = false, length = 30)
    private String orderId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType orderType = OrderType.PICKUP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Priority priority = Priority.NORMAL;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime orderTime = LocalDateTime.now();

    @Column
    private LocalDateTime readyTime;

    @Column
    private LocalDateTime completedTime;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus { PENDING, PREPARING, READY, COMPLETED, CANCELLED }
    public enum OrderType { PICKUP, DELIVERY }
    public enum Priority { LOW, NORMAL, HIGH, URGENT }

    // Helper methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    @PrePersist
    protected void onCreate() {
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Method to calculate total amount from items
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}