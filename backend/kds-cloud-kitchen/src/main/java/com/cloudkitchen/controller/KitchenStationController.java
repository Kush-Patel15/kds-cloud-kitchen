package com.cloudkitchen.controller;

import com.cloudkitchen.model.KitchenStation;
import com.cloudkitchen.model.KitchenStation.StationType;
import com.cloudkitchen.service.KitchenStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
public class KitchenStationController {

    @Autowired
    private KitchenStationService kitchenStationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStations() {
        try {
            List<KitchenStation> stations = kitchenStationService.getAllStations();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stations", stations,
                "count", stations.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch stations: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveStations() {
        try {
            List<KitchenStation> stations = kitchenStationService.getActiveStations();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stations", stations,
                "count", stations.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch active stations: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getStationsByType(@PathVariable StationType type) {
        try {
            List<KitchenStation> stations = kitchenStationService.getStationsByType(type);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stations", stations,
                "count", stations.size(),
                "type", type
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch stations by type: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStationById(@PathVariable Long id) {
        try {
            KitchenStation station = kitchenStationService.getStationById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "station", station
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createStation(@RequestBody KitchenStation station) {
        try {
            KitchenStation savedStation = kitchenStationService.createStation(station);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "station", savedStation,
                "message", "Kitchen station created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create station: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStation(@PathVariable Long id, @RequestBody KitchenStation station) {
        try {
            KitchenStation updatedStation = kitchenStationService.updateStation(id, station);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "station", updatedStation,
                "message", "Kitchen station updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update station: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStationStatus(@PathVariable Long id, 
                                                                  @RequestBody Map<String, Boolean> statusUpdate) {
        try {
            Boolean isActive = statusUpdate.get("isActive");
            KitchenStation updatedStation = kitchenStationService.updateStationStatus(id, isActive);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "station", updatedStation,
                "message", "Station status updated"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update station status: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStation(@PathVariable Long id) {
        try {
            kitchenStationService.deleteStation(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kitchen station deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete station: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/workload")
    public ResponseEntity<Map<String, Object>> getStationWorkload() {
        try {
            Map<String, Object> workload = kitchenStationService.getStationWorkload();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "workload", workload
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch station workload: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getStationTypes() {
        try {
            StationType[] types = StationType.values();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "types", types
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch station types: " + e.getMessage()
            ));
        }
    }
}