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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    // List active (non‑completed / non‑cancelled) orders
    public List<Order> listActiveOrders() {
        return orderRepository.findByStatusInOrderByOrderTimeAsc(
                List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY));
    }

    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(generateOrderCode());
        }
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderTimeAsc(status);
    }

    public List<Order> getActiveOrders() {
        return listActiveOrders();
    }

    public List<Order> getTodaysOrders() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return orderRepository.findByOrderTimeBetweenOrderByOrderTimeDesc(start, end);
    }

    public List<Order> getOrdersByCustomerPhone(String phone) {
        return orderRepository.findByCustomerPhoneOrderByOrderTimeDesc(phone);
    }

    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderTimeDesc(email);
    }

    // Update editable fields that actually exist
    public Order updateOrder(Long id, Order details) {
        Order o = getOrderById(id);
        o.setCustomerName(details.getCustomerName());
        o.setCustomerPhone(details.getCustomerPhone());
        o.setCustomerEmail(details.getCustomerEmail());
        o.setStatus(details.getStatus());
        o.setOrderType(details.getOrderType());
        o.setTotalAmount(details.getTotalAmount());
        o.setPriority(details.getPriority());
        o.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(o);
    }

    public Order updateStatus(Long id, OrderStatus status) {
        Order o = getOrderById(id);
        OrderStatus prev = o.getStatus();
        o.setStatus(status);
        o.setUpdatedAt(LocalDateTime.now());

        if (status == OrderStatus.READY && prev != OrderStatus.READY) {
            o.setReadyTime(LocalDateTime.now());
        }
        if (status == OrderStatus.COMPLETED) {
            // nothing special stored (no actualCompletionTime field) – updatedAt already set
        }
        return orderRepository.save(o);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public Map<String, Long> getOrderCountsByStatus() {
        Map<String, Long> map = new HashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            map.put(s.name(), orderRepository.countByStatus(s));
        }
        return map;
    }

    public Double getRevenue(String startDate, String endDate) {
        // Simple: use today's orders; adapt if you add date filtering later
        return getTodaysOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
    }

    public Map<String, Object> getOrderSummary(String startDate, String endDate) {
        List<Order> todays = getTodaysOrders();
        Map<String,Object> summary = new HashMap<>();
        summary.put("totalOrders", todays.size());
        summary.put("completedOrders", todays.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count());
        summary.put("pendingOrders", todays.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        summary.put("totalRevenue", getRevenue(startDate, endDate));
        return summary;
    }

    public Double getAverageCompletionTime() {
        List<Order> completed = getOrdersByStatus(OrderStatus.COMPLETED);
        if (completed.isEmpty()) return 0.0;

        return completed.stream()
                .map(o -> {
                    LocalDateTime end = (o.getReadyTime() != null) ? o.getReadyTime() : o.getUpdatedAt();
                    return end != null ? Duration.between(o.getOrderTime(), end).toMinutes() : 0;
                })
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    @Transactional
    public Order createOrderFromPayload(List<Map<String,Object>> itemsPayload,
                                        String customerName,
                                        String customerPhone,
                                        String customerEmail,
                                        String orderTypeStr) {
        if (itemsPayload == null || itemsPayload.isEmpty())
            throw new IllegalArgumentException("No items provided");

        OrderType orderType;
        try {
            orderType = OrderType.valueOf(orderTypeStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid orderType: " + orderTypeStr);
        }

        Order order = new Order();
        order.setOrderId(generateOrderCode());
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerEmail(customerEmail);
        order.setOrderType(orderType);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (Map<String,Object> raw : itemsPayload) {
            Long menuItemId = ((Number) raw.get("menuItemId")).longValue();
            Integer qty = ((Number) raw.get("quantity")).intValue();
            if (qty == null || qty <= 0) throw new IllegalArgumentException("Quantity must be > 0");

            MenuItem mi = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new IllegalArgumentException("MenuItem not found id=" + menuItemId));

            OrderItem oi = new OrderItem();
            oi.setMenuItem(mi);
            oi.setQuantity(qty);
            oi.setUnitPrice(mi.getPrice());
            oi.setStatus(OrderItem.ItemStatus.PENDING);

            order.addItem(oi);
            total = total.add(mi.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    private String generateOrderCode() {
        return "O" + (System.currentTimeMillis() % 1_000_000);
    }
}