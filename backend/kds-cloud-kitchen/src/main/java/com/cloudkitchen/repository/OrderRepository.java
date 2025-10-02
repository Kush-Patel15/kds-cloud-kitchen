package com.cloudkitchen.repository;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.model.Order.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByStatusOrderByOrderTimeAsc(OrderStatus status);
    List<Order> findByStatusInOrderByOrderTimeAsc(List<OrderStatus> statuses);
    List<Order> findByPriorityOrderByOrderTimeAsc(Priority priority);
    List<Order> findByCustomerPhoneOrderByOrderTimeDesc(String customerPhone);
    List<Order> findByOrderTimeBetweenOrderByOrderTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    long countByStatus(OrderStatus status);
    long countByStatusAndOrderTimeBetween(OrderStatus status, LocalDateTime startTime, LocalDateTime endTime);
}