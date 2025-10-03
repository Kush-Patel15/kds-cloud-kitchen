package com.cloudkitchen.service;

import com.cloudkitchen.model.MenuItem;
import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.model.Order.OrderType;
import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.repository.MenuItemRepository;
import com.cloudkitchen.repository.OrderItemRepository;
import com.cloudkitchen.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    // List active (non‑completed / non‑cancelled) orders
    public List<Order> listActiveOrders() {
        return orderRepository.findByStatusInOrderByOrderTimeAsc(
            Arrays.asList(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY)
        );
    }

    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order createOrder(Order order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(generateOrderId());
        }
        
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now());
        }
        
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(LocalDateTime.now());
        }
        
        order.calculateTotalAmount();
        return orderRepository.save(order);
    }

    @Transactional
    public Order createOrderFromPayload(List<Map<String, Object>> items, String customerName, 
                                      String customerPhone, String customerEmail, String orderType) {
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerEmail(customerEmail);
        order.setOrderType(OrderType.valueOf(orderType));
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> itemData : items) {
            Long menuItemId = Long.valueOf(itemData.get("menuItemId").toString());
            Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(menuItem.getPrice());

            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotal);

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with orderId: " + orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderTimeAsc(status);
    }

    public List<Order> getActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
            OrderStatus.PENDING, 
            OrderStatus.PREPARING, 
            OrderStatus.READY
        );
        return orderRepository.findByStatusInOrderByOrderTimeAsc(activeStatuses);
    }

    public List<Order> getTodaysOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return orderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(startOfDay, endOfDay);
    }

    public List<Order> getOrdersByCustomerPhone(String phone) {
        return orderRepository.findByCustomerPhoneOrderByOrderTimeDesc(phone);
    }

    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderTimeDesc(email);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        if (status == OrderStatus.READY) {
            order.setReadyTime(LocalDateTime.now());
        } else if (status == OrderStatus.COMPLETED) {
            order.setCompletedTime(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrder) {
        Order existingOrder = getOrderById(orderId);
        
        // Update fields
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        if (updatedOrder.getReadyTime() != null) {
            existingOrder.setReadyTime(updatedOrder.getReadyTime());
        }
        
        if (updatedOrder.getCompletedTime() != null) {
            existingOrder.setCompletedTime(updatedOrder.getCompletedTime());
        }
        
        return orderRepository.save(existingOrder);
    }

    // ==================== MISSING METHODS FOR ANALYTICS ====================
    
    /**
     * Get order counts grouped by status
     */
    public Map<String, Long> getOrderCountsByStatus() {
        Map<String, Long> counts = new HashMap<>();
        
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            counts.put(status.name(), count);
        }
        
        return counts;
    }

    /**
     * Get revenue for a date range (if dates are null, get all-time revenue)
     */
    public Map<String, Object> getRevenue(String startDate, String endDate) {
        try {
            List<Order> orders;
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
                orders = orderRepository.findByOrderTimeBetween(start, end);
            } else {
                orders = orderRepository.findAll();
            }

            BigDecimal totalRevenue = orders.stream()
                .map(this::safeOrderTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

            return Map.of(
                "totalRevenue", totalRevenue,
                "currency", "USD",
                "period", (startDate != null && endDate != null) ? (startDate + " to " + endDate) : "All time"
            );
        } catch (Exception e) {
            return Map.of(
                "totalRevenue", BigDecimal.ZERO,
                "currency", "USD",
                "period", "Error calculating revenue",
                "error", e.getMessage()
            );
        }
    }

    /**
     * Get order summary for a date range
     */
    public Map<String, Object> getOrderSummary(String startDate, String endDate) {
        try {
            List<Order> orders;
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
                orders = orderRepository.findByOrderTimeBetween(start, end);
            } else {
                orders = orderRepository.findAll();
            }

            long totalOrders = orders.size();
            long completedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
            long pendingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long preparingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
            long readyOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.READY).count();
            long cancelledOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

            BigDecimal totalRevenue = orders.stream()
                    .map(this::safeOrderTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal averageOrderValue = totalOrders > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return Map.of(
                "totalOrders", totalOrders,
                "completedOrders", completedOrders,
                "pendingOrders", pendingOrders,
                "preparingOrders", preparingOrders,
                "readyOrders", readyOrders,
                "cancelledOrders", cancelledOrders,
                "totalRevenue", totalRevenue,
                "averageOrderValue", averageOrderValue
            );
        } catch (Exception e) {
            return Map.of(
                "error", "Failed to calculate order summary: " + e.getMessage(),
                "totalOrders", 0L,
                "totalRevenue", BigDecimal.ZERO
            );
        }
    }

    /**
     * Get average completion time for orders
     */
    public Map<String, Object> getAverageCompletionTime() {
        try {
            List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && 
                            o.getCompletedTime() != null && 
                            o.getOrderTime() != null)
                .collect(Collectors.toList());
            
            if (completedOrders.isEmpty()) {
                return Map.of(
                    "averageMinutes", 0.0,
                    "completedOrdersCount", 0L,
                    "message", "No completed orders found"
                );
            }
            
            double totalMinutes = completedOrders.stream()
                .mapToDouble(order -> {
                    Duration duration = Duration.between(order.getOrderTime(), order.getCompletedTime());
                    return duration.toMinutes();
                })
                .sum();
            
            double averageMinutes = totalMinutes / completedOrders.size();
            
            return Map.of(
                "averageMinutes", Math.round(averageMinutes * 100.0) / 100.0,
                "completedOrdersCount", (long) completedOrders.size(),
                "totalMinutes", Math.round(totalMinutes * 100.0) / 100.0
            );
        } catch (Exception e) {
            return Map.of(
                "error", "Failed to calculate average completion time: " + e.getMessage(),
                "averageMinutes", 0.0,
                "completedOrdersCount", 0L
            );
        }
    }

    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    private BigDecimal safeOrderTotal(Order o) {
        if (o.getTotalAmount() != null) return o.getTotalAmount();
        if (o.getItems() == null) return BigDecimal.ZERO;
        return o.getItems().stream()
                .map(i -> {
                    BigDecimal unit = i.getUnitPrice() != null ? i.getUnitPrice() :
                            (i.getMenuItem() != null && i.getMenuItem().getPrice() != null
                                    ? i.getMenuItem().getPrice()
                                    : BigDecimal.ZERO);
                    return unit.multiply(BigDecimal.valueOf(i.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}