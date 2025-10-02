package com.cloudkitchen.controller;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.service.BroadcastService;
import com.cloudkitchen.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;
    private final BroadcastService ws;

    public OrderController(OrderService orderService, BroadcastService ws) {
        this.orderService = orderService;
        this.ws = ws;
    }

    @PostMapping
    public ResponseEntity<Map<String,Object>> create(@RequestBody Map<String,Object> body) {
        try {
            List<Map<String,Object>> items = (List<Map<String,Object>>) body.get("items");
            String customerName = (String) body.get("customerName");
            String customerPhone = (String) body.get("customerPhone");
            String orderType = (String) body.get("orderType");

            if (customerName == null || customerName.trim().isEmpty()) {
                customerName = "Guest Customer";
            }
            if (customerPhone == null) {
                customerPhone = "";
            }
            if (orderType == null) {
                orderType = "pickup";
            }
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Order items are required"
                ));
            }

            Order saved = orderService.createOrderFromPayload(items, customerName, customerPhone, orderType);
            Map<String,Object> mapped = mapOrder(saved);

            // Broadcast new order to kitchen
            if (ws != null) {
                ws.send("/topic/orders", Map.of("type","created","order", mapped));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", mapped,
                "message", "Order placed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create order: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String,Object>> active() {
        try {
            List<Order> orders = orderService.listActiveOrders();
            List<Map<String,Object>> mapped = orders.stream().map(this::mapOrder).toList();
            return ResponseEntity.ok(Map.of("success", true, "orders", mapped));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch active orders: " + e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String,Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String,String> body) {
        try {
            String status = body.get("status");
            Order updated = orderService.updateStatus(id, OrderStatus.valueOf(status));
            Map<String,Object> mapped = mapOrder(updated);
            
            // Broadcast status update
            if (ws != null) {
                ws.send("/topic/orders", Map.of("type","status","order", mapped));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "order", mapped));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<Map<String,Object>> getByOrderCode(@PathVariable String orderCode) {
        try {
            Order order = orderService.getOrderByOrderId(orderCode);
            Map<String,Object> mapped = mapOrder(order);
            return ResponseEntity.ok(Map.of("success", true, "order", mapped));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Order not found: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            Map<String,Object> mapped = mapOrder(order);
            return ResponseEntity.ok(Map.of("success", true, "order", mapped));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Order not found: " + e.getMessage()
            ));
        }
    }

    private Map<String,Object> mapOrder(Order order) {
        return Map.of(
            "id", order.getId(),
            "orderId", order.getOrderId() != null ? order.getOrderId() : "",
            "customerName", order.getCustomerName() != null ? order.getCustomerName() : "",
            "customerPhone", order.getCustomerPhone() != null ? order.getCustomerPhone() : "",
            "orderType", order.getOrderType() != null ? order.getOrderType().name() : "PICKUP",
            "status", order.getStatus().name(),
            "totalAmount", order.getTotalAmount(),
            "createdAt", order.getCreatedAt().toString(),
            "orderTime", order.getOrderTime() != null ? order.getOrderTime().toString() : order.getCreatedAt().toString(),
            "items", order.getItems().stream().map(item -> Map.of(
                "id", item.getId(),
                "menuItemId", item.getMenuItem().getId(),
                "name", item.getMenuItem().getName(),
                "quantity", item.getQuantity(),
                "price", item.getUnitPrice(),
                "category", item.getMenuItem().getCategory() != null ? item.getMenuItem().getCategory().name() : "MISC"
            )).toList()
        );
    }
}