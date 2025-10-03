package com.cloudkitchen.controller;

import com.cloudkitchen.service.ReportExportService;
import com.cloudkitchen.service.ReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService exportService;

    public ReportController(ReportService reportService, ReportExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String,Object>> daily(@RequestParam String date) {
        var data = reportService.buildDailyReport(LocalDate.parse(date));
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String,Object>> weekly(@RequestParam String startDate,
                                                     @RequestParam String endDate) {
        var data = reportService.buildRangeReport(LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String,Object>> monthly(@RequestParam int month,
                                                      @RequestParam int year) {
        var data = reportService.buildMonthlyReport(year, month);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/download/{type}")
    public ResponseEntity<byte[]> download(@PathVariable String type,
                                           @RequestParam Map<String,String> params,
                                           @RequestParam(defaultValue = "pdf") String format) {

        Map<String,Object> payload = switch (type) {
            case "daily" -> reportService.buildDailyReport(LocalDate.parse(params.get("date")));
            case "weekly" -> reportService.buildRangeReport(
                    LocalDate.parse(params.get("startDate")),
                    LocalDate.parse(params.get("endDate")));
            case "monthly" -> reportService.buildMonthlyReport(
                    Integer.parseInt(params.get("year")),
                    Integer.parseInt(params.get("month")));
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        };

        byte[] bytes;
        MediaType mediaType;
        String filename;
        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format) || "csv".equalsIgnoreCase(format)) {
            bytes = exportService.exportExcel(type, payload);
            mediaType = MediaType.TEXT_PLAIN;
            filename = type + "-report.csv";
        } else {
            bytes = exportService.exportPdf(type, payload);
            mediaType = MediaType.APPLICATION_PDF;
            filename = type + "-report.pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .contentLength(bytes.length)
                .body(bytes);
    }
}