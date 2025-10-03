package com.cloudkitchen.repository;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.model.Order.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByStatusOrderByOrderTimeAsc(OrderStatus status);
    List<Order> findByStatusInOrderByOrderTimeAsc(List<OrderStatus> statuses);
    List<Order> findByPriorityOrderByOrderTimeAsc(Priority priority);
    List<Order> findByCustomerPhoneOrderByOrderTimeDesc(String customerPhone);
    List<Order> findByCustomerEmailOrderByOrderTimeDesc(String customerEmail);
    List<Order> findByOrderTimeBetweenOrderByOrderTimeDesc(LocalDateTime start, LocalDateTime end);

    // For reports (load raw orders then aggregate in service)
    List<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.orderTime ASC")
    List<Order> findActiveOrders(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.customerEmail = :email AND o.status IN :statuses")
    List<Order> findByCustomerEmailAndStatusIn(@Param("email") String email,
                                               @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderTime >= :startDate AND o.orderTime <= :endDate AND o.status = 'COMPLETED'")
    List<Order> findCompletedOrdersBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // REMOVED:
    // Double findTotalRevenueBetween(...)
    // Double findAveragePreparationTimeBetween(...)
    // List<Object[]> findPeakHoursBetween(...)
}