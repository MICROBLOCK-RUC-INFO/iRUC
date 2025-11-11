package com.example.fooddelivery.repository;


import com.example.fooddelivery.domain.FoodDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodDeliveryOrderRepository extends JpaRepository<FoodDeliveryOrder, String> {

    Optional<FoodDeliveryOrder> findByStationFoodStoreId(String stationFoodStoreId);
}
