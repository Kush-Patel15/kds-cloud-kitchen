package com.cloudkitchen.service;

import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.model.OrderItem.ItemStatus;
import com.cloudkitchen.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
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

    public OrderItem updateOrderItem(Long id, OrderItem orderItemDetails) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));
        orderItem.setQuantity(orderItemDetails.getQuantity());
        orderItem.setUnitPrice(orderItemDetails.getUnitPrice());
        orderItem.setTotalPrice(orderItemDetails.getTotalPrice());
        orderItem.setSpecialInstructions(orderItemDetails.getSpecialInstructions());
        orderItem.setStatus(orderItemDetails.getStatus());
        return orderItemRepository.save(orderItem);
    }

    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }
}