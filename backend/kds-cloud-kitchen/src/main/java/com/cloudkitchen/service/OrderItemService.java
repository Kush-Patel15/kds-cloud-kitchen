package com.cloudkitchen.service;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.model.OrderItem.ItemStatus;
import com.cloudkitchen.repository.OrderItemRepository;
import com.cloudkitchen.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    public OrderItem createOrderItem(OrderItem orderItem) {
        OrderItem saved = orderItemRepository.save(orderItem);
        recalcOrderTotal(saved.getOrder());
        return saved;
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));
    }

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> getOrderItemsByStatus(ItemStatus status) {
        return orderItemRepository.findByStatus(status);
    }

    public List<OrderItem> getOrderItemsByMenuItemId(Long menuItemId) {
        return orderItemRepository.findByMenuItemId(menuItemId);
    }

    public OrderItem updateOrderItem(Long id, OrderItem details) {
        OrderItem oi = orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        if (details.getQuantity() != null) {
            oi.setQuantity(details.getQuantity());
        }
        if (details.getUnitPrice() != null) {
            oi.setUnitPrice(details.getUnitPrice());
        }
        if (details.getStatus() != null) {
            oi.setStatus(details.getStatus());
        }
        if (details.getMenuItem() != null) {
            oi.setMenuItem(details.getMenuItem());
        }

        OrderItem saved = orderItemRepository.save(oi);
        recalcOrderTotal(saved.getOrder());
        return saved;
    }

    public void deleteOrderItem(Long id) {
        OrderItem oi = orderItemRepository.findById(id)
                .orElse(null);
        orderItemRepository.deleteById(id);
        if (oi != null) {
            recalcOrderTotal(oi.getOrder());
        }
    }

    private void recalcOrderTotal(Order order) {
        if (order == null) return;
        BigDecimal total = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}