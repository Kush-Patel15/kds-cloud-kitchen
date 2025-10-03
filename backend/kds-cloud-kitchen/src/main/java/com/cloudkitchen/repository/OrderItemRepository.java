package com.cloudkitchen.repository;

import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.model.OrderItem.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByStatus(ItemStatus status);
    List<OrderItem> findByMenuItemId(Long menuItemId);
    
    // Additional methods for reports functionality - Fixed queries
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE o.orderTime BETWEEN :startDate AND :endDate")
    List<OrderItem> findByOrderTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("""
        SELECT mi.name,
               SUM(oi.quantity) as qty,
               SUM(oi.quantity * oi.unitPrice) as revenue
        FROM OrderItem oi
          JOIN oi.order o
          JOIN oi.menuItem mi
        WHERE o.orderTime BETWEEN :start AND :end
        GROUP BY mi.id, mi.name
        ORDER BY qty DESC
    """)
    List<Object[]> findTopSellingItemsBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    
    @Query("SELECT mi.name, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
           "JOIN oi.order o JOIN oi.menuItem mi WHERE o.orderTime BETWEEN :startDate AND :endDate " +
           "GROUP BY mi.name ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingItemsWithQuantityBetween(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(DISTINCT mi.name) FROM OrderItem oi JOIN oi.order o JOIN oi.menuItem mi WHERE o.orderTime BETWEEN :startDate AND :endDate")
    Long countDistinctItemsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT mi.name, COUNT(oi) as orderCount FROM OrderItem oi " +
           "JOIN oi.order o JOIN oi.menuItem mi WHERE o.orderTime BETWEEN :startDate AND :endDate " +
           "GROUP BY mi.name ORDER BY orderCount DESC")
    List<Object[]> findMostOrderedItemsBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}