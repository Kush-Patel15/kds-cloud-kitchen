package com.cloudkitchen.repository;

import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.model.OrderItem.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByStatus(ItemStatus status);
    List<OrderItem> findByMenuItemId(Long menuItemId);
}