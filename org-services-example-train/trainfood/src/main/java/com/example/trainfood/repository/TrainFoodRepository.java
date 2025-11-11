package com.example.trainfood.repository;

import com.example.trainfood.domain.TrainFood;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TrainFoodRepository extends JpaRepository<TrainFood, String> {

    // 根据 Trip ID 查询 TrainFood
    TrainFood findByTripId(String tripId);
}
