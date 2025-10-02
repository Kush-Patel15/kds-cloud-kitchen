package com.cloudkitchen.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "is_dismissed", nullable = false)
    private Boolean isDismissed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitchen_station_id")
    private KitchenStation kitchenStation;

    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
        if (isDismissed == null) {
            isDismissed = false;
        }
        if (priority == null) {
            priority = Priority.NORMAL;
        }
    }

    public enum NotificationType {
        ORDER_RECEIVED, ORDER_DELAYED, ORDER_READY, ORDER_COMPLETED, 
        EQUIPMENT_FAILURE, INVENTORY_LOW, STAFF_NEEDED, PEAK_HOURS,
        SYSTEM_ALERT, CUSTOMER_UPDATE
    }

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
}