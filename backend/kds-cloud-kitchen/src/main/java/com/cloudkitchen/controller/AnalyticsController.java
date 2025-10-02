package com.cloudkitchen.controller;

import com.cloudkitchen.service.OrderService;
import com.cloudkitchen.service.MenuItemService;
import com.cloudkitchen.service.KitchenStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private KitchenStationService kitchenStationService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        try {
            Map<String, Object> analytics = Map.of(
                "orderCounts", orderService.getOrderCountsByStatus(),
                "revenue", orderService.getRevenue(null, null),
                "popularItems", menuItemService.getPopularMenuItems(),
                "stationWorkload", kitchenStationService.getStationWorkload()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "analytics", analytics
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch dashboard analytics: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/orders/summary")
    public ResponseEntity<Map<String, Object>> getOrderSummary(@RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> summary = orderService.getOrderSummary(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "summary", summary
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch order summary: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getKitchenPerformance() {
        try {
            Map<String, Object> performance = Map.of(
                "stationWorkload", kitchenStationService.getStationWorkload(),
                "orderCounts", orderService.getOrderCountsByStatus(),
                "averageCompletionTime", orderService.getAverageCompletionTime()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "performance", performance
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch kitchen performance: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/menu/insights")
    public ResponseEntity<Map<String, Object>> getMenuInsights(@RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> insights = menuItemService.getMenuInsights(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "insights", insights
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch menu insights: " + e.getMessage()
            ));
        }
    }
}