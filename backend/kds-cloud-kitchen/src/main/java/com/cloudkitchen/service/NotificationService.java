package com.cloudkitchen.service;

import com.cloudkitchen.model.Notification;
import com.cloudkitchen.model.Notification.NotificationType;
import com.cloudkitchen.model.Notification.Priority;
import com.cloudkitchen.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc();
    }

    public List<Notification> getUrgentNotifications() {
        return notificationRepository.findByPriorityOrderByCreatedAtDesc(Priority.URGENT);
    }

    public List<Notification> getNotificationsByType(NotificationType type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    public List<Notification> getNotificationsByOrder(Long orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public Notification dismissNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsDismissed(true);
        return notificationRepository.save(notification);
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalseAndIsDismissedFalse();
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}