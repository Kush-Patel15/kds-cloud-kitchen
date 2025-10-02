package com.cloudkitchen.controller;

import com.cloudkitchen.model.Notification;
import com.cloudkitchen.model.Notification.NotificationType;
import com.cloudkitchen.model.Notification.Priority;
import com.cloudkitchen.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "count", notifications.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notifications: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "count", notifications.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch unread notifications: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/urgent")
    public ResponseEntity<Map<String, Object>> getUrgentNotifications() {
        try {
            List<Notification> notifications = notificationService.getUrgentNotifications();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "count", notifications.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch urgent notifications: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(@PathVariable NotificationType type) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(type);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "count", notifications.size(),
                "type", type
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notifications by type: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getNotificationsByOrder(@PathVariable Long orderId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByOrder(orderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notifications", notifications,
                "count", notifications.size(),
                "orderId", orderId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notifications for order: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long id) {
        try {
            Notification notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notification", notification
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Notification notification) {
        try {
            Notification savedNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notification", savedNotification,
                "message", "Notification created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create notification: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notification", notification,
                "message", "Notification marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark notification as read: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissNotification(@PathVariable Long id) {
        try {
            Notification notification = notificationService.dismissNotification(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "notification", notification,
                "message", "Notification dismissed"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to dismiss notification: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete notification: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch unread count: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getNotificationTypes() {
        try {
            NotificationType[] types = NotificationType.values();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "types", types
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notification types: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/priorities")
    public ResponseEntity<Map<String, Object>> getNotificationPriorities() {
        try {
            Priority[] priorities = Priority.values();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "priorities", priorities
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch notification priorities: " + e.getMessage()
            ));
        }
    }
}