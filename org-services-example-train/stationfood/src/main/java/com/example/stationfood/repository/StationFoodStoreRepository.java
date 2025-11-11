package com.example.stationfood.repository;


import com.example.stationfood.domain.StationFoodStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationFoodStoreRepository extends JpaRepository<StationFoodStore, String> {

    List<StationFoodStore> findByStationNameIn(List<String> stationNames);
}
