package com.cloudkitchen.service;

import com.cloudkitchen.model.Order;
import com.cloudkitchen.model.OrderItem;
import com.cloudkitchen.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Map<String,Object> buildDailyReport(LocalDate date) {
        return buildRangeReport(date, date);
    }

    public Map<String,Object> buildMonthlyReport(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return buildRangeReport(start, end);
    }

    public Map<String,Object> buildRangeReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23,59,59);

        // Load all orders in range
        List<Order> orders = orderRepository.findByOrderTimeBetween(start, end);

        long totalOrders = orders.size();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<Long> prepMinutes = new ArrayList<>();
        Map<String, ItemAgg> itemMap = new HashMap<>();
        Map<Integer, Long> hourCounts = new TreeMap<>();

        for (Order o : orders) {
            LocalDateTime placed = o.getOrderTime(); // adjust if different field
            LocalDateTime ready = extractReady(o);

            if (placed != null) {
                hourCounts.merge(placed.getHour(), 1L, Long::sum);
            }
            if (placed != null && ready != null && !ready.isBefore(placed)) {
                prepMinutes.add(ChronoUnit.MINUTES.between(placed, ready));
            }

            if (o.getItems() != null) {
                for (OrderItem it : o.getItems()) {
                    long qty = it.getQuantity();
                    BigDecimal price = extractPrice(it);
                    BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
                    totalRevenue = totalRevenue.add(line);

                    String name = extractName(it);
                    itemMap.computeIfAbsent(name, k -> new ItemAgg()).add(qty, line);
                }
            }
        }

        double avgPrep = prepMinutes.isEmpty()
                ? 0d
                : Math.round(prepMinutes.stream().mapToLong(Long::longValue).average().orElse(0d) * 10.0) / 10.0;

        List<Map<String,Object>> topItems = itemMap.entrySet().stream()
                .sorted((a,b)-> Long.compare(b.getValue().qty, a.getValue().qty))
                .limit(10)
                .map(e -> Map.of(
                        "name", (Object) e.getKey(),
                        "quantity", (Object) e.getValue().qty,
                        "revenue", (Object) e.getValue().rev.setScale(2, java.math.RoundingMode.HALF_UP)
                ))
                .collect(Collectors.toList());

        List<Map<String,Object>> peakHours = hourCounts.entrySet().stream()
                .map(e -> Map.of(
                        "time", (Object) String.format("%02d:00 - %02d:59", e.getKey(), e.getKey()),
                        "orders", (Object) e.getValue()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "startDate", startDate.toString(),
                "endDate", endDate.toString(),
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue.setScale(2, BigDecimal.ROUND_HALF_UP),
                "avgPrepTime", avgPrep,
                "topItems", topItems,
                "peakHours", peakHours
        );
    }

    private LocalDateTime extractReady(Order o) {
        // adjust to real field name (readyTime / completedAt / updatedAt)
        try { return o.getReadyTime(); } catch (Exception ignored) {}
        return null;
    }

    private BigDecimal extractPrice(OrderItem it) {
        if (it.getUnitPrice() != null) return it.getUnitPrice();
        if (it.getMenuItem() != null && it.getMenuItem().getPrice() != null)
            return it.getMenuItem().getPrice();
        return BigDecimal.ZERO;
    }

    private String extractName(OrderItem it) {
        try {
            var menu = it.getMenuItem();
            if (menu != null) {
                var nm = menu.getClass().getMethod("getName");
                Object v = nm.invoke(menu);
                return String.valueOf(v);
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    private static class ItemAgg {
        long qty = 0;
        BigDecimal rev = BigDecimal.ZERO;
        void add(long q, BigDecimal r) { qty += q; rev = rev.add(r); }
    }
}