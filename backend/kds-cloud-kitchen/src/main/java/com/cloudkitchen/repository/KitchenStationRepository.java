package com.cloudkitchen.repository;

import com.cloudkitchen.model.KitchenStation;
import com.cloudkitchen.model.KitchenStation.StationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenStationRepository extends JpaRepository<KitchenStation, Long> {
    List<KitchenStation> findByIsActiveTrueOrderByName();
    List<KitchenStation> findByTypeAndIsActiveTrueOrderByName(StationType type);
    KitchenStation findByNameAndIsActiveTrue(String name);
}