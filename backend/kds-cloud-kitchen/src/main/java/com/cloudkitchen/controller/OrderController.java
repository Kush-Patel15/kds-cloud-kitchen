package com.cloudkitchen.controller;

import com.cloudkitchen.controller.dto.OrderCreateRequest;
import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.Order.OrderStatus;
import com.cloudkitchen.model.User;
import com.cloudkitchen.service.BroadcastService;
import com.cloudkitchen.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final BroadcastService ws;

    public OrderController(OrderService orderService, BroadcastService ws) {
        this.orderService = orderService;
        this.ws = ws;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> create(
            @RequestBody OrderCreateRequest req,
            @RequestHeader(value="X-User-Email", required=false) String emailHdr,
            HttpServletRequest request) {

        System.out.println("=== CREATE ORDER ===");
        System.out.println("Email header: " + emailHdr);
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Payload: " + req);

        String userEmail = (emailHdr != null && !emailHdr.isBlank())
                ? emailHdr.trim()
                : getUserEmailFromRequest(request);

        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "You must be logged in to place an order"));
        }

        if (req.items() == null || req.items().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Order items are required"));
        }

        String orderType = (req.orderType() == null ? "PICKUP" : req.orderType()).toUpperCase();

        // Convert DTO items to the structure your service expects
        var itemMaps = req.items().stream()
                .map(i -> Map.<String,Object>of(
                        "menuItemId", i.menuItemId(),
                        "quantity", i.quantity()))
                .toList();

        Order order;
        try {
            order = orderService.createOrderFromPayload(
                    itemMaps,
                    req.customerName(),
                    req.customerPhone(),
                    userEmail,          // <== make sure this is passed
                    orderType
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }

        Map<String,Object> response = Map.of(
                "success", true,
                "message", "Order created successfully",
                "orderId", order.getOrderId(),
                "id", order.getId(),
                "order", mapOrder(order)
        );

        try {
            ws.send("/topic/orders", Map.of("type","created","order", mapOrder(order)));
        } catch (Exception ex) {
            System.err.println("Broadcast failed: " + ex.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> active() {
        try {
            List<Order> activeOrders = orderService.getActiveOrders();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", activeOrders,
                "message", "Active orders retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to fetch active orders: " + e.getMessage(),
                "orders", Collections.emptyList()
            ));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            
            Order updatedOrder = orderService.updateStatus(id, status);
            
            // Broadcast status update
            ws.send("/topic/orders", Map.of("type", "status", "order", mapOrder(updatedOrder)));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", updatedOrder,
                "message", "Order status updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update order status: " + e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedTime(LocalDateTime.now());
            
            Order updatedOrder = orderService.updateOrder(id, order);
            
            // Broadcast completion
            ws.send("/topic/orders", Map.of("type", "completed", "order", mapOrder(updatedOrder)));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", updatedOrder,
                "message", "Order completed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to complete order: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<Map<String, Object>> getByOrderCode(@PathVariable String orderCode) {
        try {
            Order order = orderService.getOrderByOrderId(orderCode);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Order not found: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Order not found: " + e.getMessage()
            ));
        }
    }

    private Map<String,Object> mapOrder(Order o) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", o.getId());
        map.put("orderId", o.getOrderId());
        map.put("customerName", o.getCustomerName());
        map.put("customerPhone", o.getCustomerPhone());
        map.put("customerEmail", o.getCustomerEmail());
        map.put("orderType", o.getOrderType().name());
        map.put("status", o.getStatus().name());
        map.put("totalAmount", o.getTotalAmount());
        map.put("createdAt", o.getCreatedAt());
        map.put("orderTime", o.getOrderTime());
        map.put("updatedAt", o.getUpdatedAt());
        map.put("readyTime", o.getReadyTime());
        map.put("items", o.getItems().stream().map(it -> Map.of(
            "id", it.getId(),
            "menuItemId", it.getMenuItem().getId(),
            "name", it.getMenuItem().getName(),
            "quantity", it.getQuantity(),
            "price", it.getUnitPrice(),
            "category", it.getMenuItem().getCategory() != null ? it.getMenuItem().getCategory().name() : "MISC"
        )).toList());
        return map;
    }

    @GetMapping("/customer/my-orders")
    public ResponseEntity<Map<String,Object>> getMyOrders(HttpServletRequest request) {
        try {
            String userEmail = getUserEmailFromRequest(request);
            
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
                ));
            }
            
            List<Order> orders = orderService.getOrdersByCustomerEmail(userEmail);
            List<Map<String,Object>> mappedOrders = orders.stream()
                .map(this::mapOrder)
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", mappedOrders
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/customer/my-orders/active")
    public ResponseEntity<Map<String,Object>> getMyActiveOrders(HttpServletRequest request) {
        try {
            String userEmail = getUserEmailFromRequest(request);
            
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
                ));
            }
            
            List<Order> allOrders = orderService.getOrdersByCustomerEmail(userEmail);
            List<Order> activeOrders = allOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.CANCELLED)
                .toList();
                
            List<Map<String,Object>> mappedOrders = activeOrders.stream()
                .map(this::mapOrder)
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", mappedOrders
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    private String getUserEmailFromRequest(HttpServletRequest request) {
        // Option 1: Check X-User-Email header first
        String headerEmail = request.getHeader("X-User-Email");
        System.out.println("X-User-Email header: " + headerEmail);
        if (headerEmail != null && !headerEmail.trim().isEmpty()) {
            return headerEmail.trim();
        }
        
        // Option 2: If using session-based auth
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object user = session.getAttribute("user");
            if (user != null && user instanceof User) {
                return ((User) user).getEmail();
            }
        }
        
        return null;
    }
}