package com.cloudkitchen.service;

import com.cloudkitchen.model.KitchenStation;
import com.cloudkitchen.model.KitchenStation.StationType;
import com.cloudkitchen.repository.KitchenStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class KitchenStationService {

    @Autowired
    private KitchenStationRepository kitchenStationRepository;

    public KitchenStation createKitchenStation(KitchenStation kitchenStation) {
        return kitchenStationRepository.save(kitchenStation);
    }

    public KitchenStation createStation(KitchenStation kitchenStation) {
        return kitchenStationRepository.save(kitchenStation);
    }

    public KitchenStation getKitchenStation(Long id) {
        return kitchenStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KitchenStation not found"));
    }

    public KitchenStation getStationById(Long id) {
        return kitchenStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KitchenStation not found"));
    }

    public List<KitchenStation> getAllKitchenStations() {
        return kitchenStationRepository.findAll();
    }

    public List<KitchenStation> getAllStations() {
        return kitchenStationRepository.findAll();
    }

    public List<KitchenStation> getActiveStations() {
        return kitchenStationRepository.findByIsActiveTrueOrderByName();
    }

    public List<KitchenStation> getStationsByType(StationType type) {
        return kitchenStationRepository.findByTypeAndIsActiveTrueOrderByName(type);
    }

    public KitchenStation updateKitchenStation(Long id, KitchenStation kitchenStation) {
        kitchenStation.setId(id);
        return kitchenStationRepository.save(kitchenStation);
    }

    public KitchenStation updateStation(Long id, KitchenStation kitchenStationDetails) {
        KitchenStation kitchenStation = kitchenStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KitchenStation not found"));
        kitchenStation.setName(kitchenStationDetails.getName());
        kitchenStation.setDescription(kitchenStationDetails.getDescription());
        kitchenStation.setType(kitchenStationDetails.getType());
        kitchenStation.setIsActive(kitchenStationDetails.getIsActive());
        kitchenStation.setMaxConcurrentOrders(kitchenStationDetails.getMaxConcurrentOrders());
        kitchenStation.setAveragePrepTimeMinutes(kitchenStationDetails.getAveragePrepTimeMinutes());
        return kitchenStationRepository.save(kitchenStation);
    }

    public KitchenStation updateStationStatus(Long id, Boolean isActive) {
        KitchenStation kitchenStation = kitchenStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KitchenStation not found"));
        kitchenStation.setIsActive(isActive);
        return kitchenStationRepository.save(kitchenStation);
    }

    public void deleteKitchenStation(Long id) {
        kitchenStationRepository.deleteById(id);
    }

    public void deleteStation(Long id) {
        kitchenStationRepository.deleteById(id);
    }

    public Map<String, Object> getStationWorkload() {
        Map<String, Object> workload = new HashMap<>();
        List<KitchenStation> stations = getActiveStations();
        
        workload.put("totalStations", stations.size());
        workload.put("activeStations", stations.size());
        workload.put("stationTypes", getStationTypeCounts());
        
        return workload;
    }

    private Map<String, Long> getStationTypeCounts() {
        Map<String, Long> counts = new HashMap<>();
        for (StationType type : StationType.values()) {
            long count = kitchenStationRepository.findByTypeAndIsActiveTrueOrderByName(type).size();
            counts.put(type.name(), count);
        }
        return counts;
    }
}