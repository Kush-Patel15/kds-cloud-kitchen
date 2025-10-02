package com.cloudkitchen.service;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.model.MenuItem;
import com.cloudkitchen.repository.OrderRepository;
import com.cloudkitchen.repository.OrderItemRepository;
import com.cloudkitchen.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.math.BigDecimal;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MenuItemService menuItemService;


    // ✅ Add this missing method
    public List<Order> listActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY);
        return orderRepository.findByStatusInOrderByOrderTimeAsc(activeStatuses);
    }

    // ✅ Add this missing method
    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId("ORD-" + System.currentTimeMillis());
        }
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderTimeAsc(status);
    }

    public List<Order> getActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY);
        return orderRepository.findByStatusInOrderByOrderTimeAsc(activeStatuses);
    }

    public List<Order> getTodaysOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return orderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(startOfDay, endOfDay);
    }

    public List<Order> getOrdersByCustomerPhone(String phone) {
        return orderRepository.findByCustomerPhoneOrderByOrderTimeDesc(phone);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setCustomerName(orderDetails.getCustomerName());
        order.setCustomerPhone(orderDetails.getCustomerPhone());
        order.setCustomerEmail(orderDetails.getCustomerEmail());
        order.setStatus(orderDetails.getStatus());
        order.setOrderType(orderDetails.getOrderType());
        order.setTotalAmount(orderDetails.getTotalAmount());
        order.setSpecialInstructions(orderDetails.getSpecialInstructions());
        order.setDeliveryAddress(orderDetails.getDeliveryAddress());
        order.setPriority(orderDetails.getPriority());
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        
        if (newStatus == OrderStatus.COMPLETED) {
            order.setActualCompletionTime(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public Map<String, Long> getOrderCountsByStatus() {
        Map<String, Long> counts = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status.name(), orderRepository.countByStatus(status));
        }
        return counts;
    }

    public Double getRevenue(String startDate, String endDate) {
        return getTodaysOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    public Map<String, Object> getOrderSummary(String startDate, String endDate) {
        Map<String, Object> summary = new HashMap<>();
        List<Order> orders = getTodaysOrders();
        
        summary.put("totalOrders", orders.size());
        summary.put("completedOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count());
        summary.put("pendingOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        summary.put("totalRevenue", getRevenue(startDate, endDate));
        
        return summary;
    }

    public Double getAverageCompletionTime() {
        List<Order> completedOrders = getOrdersByStatus(OrderStatus.COMPLETED);
        if (completedOrders.isEmpty()) {
            return 0.0;
        }
        
        return completedOrders.stream()
                .filter(order -> order.getActualCompletionTime() != null)
                .mapToLong(order -> java.time.Duration.between(order.getOrderTime(), order.getActualCompletionTime()).toMinutes())
                .average()
                .orElse(0.0);
    }

    public Order createOrderFromPayload(List<Map<String,Object>> items, String customerName, String customerPhone, String orderType) {
        Order order = new Order();
        
        order.setOrderId(generateOrderId());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        
        if (orderType != null) {
            try {
                order.setOrderType(Order.OrderType.valueOf(orderType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                order.setOrderType(Order.OrderType.PICKUP);
            }
        } else {
            order.setOrderType(Order.OrderType.PICKUP);
        }
        
        // ✅ Set initial status to PENDING (this will show in "Received" column)
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPriority(Order.Priority.NORMAL);
        order.setOrderTime(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Calculate total amount first
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String,Object> itemData : items) {
            Integer menuItemId = (Integer) itemData.get("menuItemId");
            Integer quantity = (Integer) itemData.get("quantity");
            
            MenuItem menuItem = menuItemService.findById(menuItemId.longValue());
            
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(itemTotal);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setTotalPrice(itemTotal);
            orderItem.setStatus(OrderItem.ItemStatus.PENDING);
            
            orderItems.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }
        orderItemRepository.saveAll(orderItems);
        
        savedOrder.setItems(orderItems);
        return savedOrder;
    }

    private String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }

    public Order updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        if (status == OrderStatus.COMPLETED) {
            order.setActualCompletionTime(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
}