package com.cloudkitchen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportDTO {
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private Double avgPrepTime;
    private List<TopItemDTO> topItems;
    private List<PeakHourDTO> peakHours;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType;

    // Constructors
    public ReportDTO() {}

    public ReportDTO(Integer totalOrders, BigDecimal totalRevenue, Double avgPrepTime) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.avgPrepTime = avgPrepTime;
    }

    // Getters and Setters
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAvgPrepTime() { return avgPrepTime; }
    public void setAvgPrepTime(Double avgPrepTime) { this.avgPrepTime = avgPrepTime; }

    public List<TopItemDTO> getTopItems() { return topItems; }
    public void setTopItems(List<TopItemDTO> topItems) { this.topItems = topItems; }

    public List<PeakHourDTO> getPeakHours() { return peakHours; }
    public void setPeakHours(List<PeakHourDTO> peakHours) { this.peakHours = peakHours; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public static class TopItemDTO {
        private String name;
        private Integer quantity;
        private BigDecimal revenue;

        public TopItemDTO() {}

        public TopItemDTO(String name, Integer quantity, BigDecimal revenue) {
            this.name = name;
            this.quantity = quantity;
            this.revenue = revenue;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class PeakHourDTO {
        private String time;
        private Integer orders;

        public PeakHourDTO() {}

        public PeakHourDTO(String time, Integer orders) {
            this.time = time;
            this.orders = orders;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public Integer getOrders() { return orders; }
        public void setOrders(Integer orders) { this.orders = orders; }
    }
}