package com.cloudkitchen.repository;

import com.cloudkitchen.model.Notification;
import com.cloudkitchen.model.Notification.NotificationType;
import com.cloudkitchen.model.Notification.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc();
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);
    List<Notification> findByPriorityOrderByCreatedAtDesc(Priority priority);
    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<Notification> findByKitchenStationIdOrderByCreatedAtDesc(Long stationId);
    long countByIsReadFalseAndIsDismissedFalse();
}