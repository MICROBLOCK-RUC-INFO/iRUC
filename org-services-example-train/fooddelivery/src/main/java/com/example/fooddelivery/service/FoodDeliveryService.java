package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.StationFoodStore;
import com.example.fooddelivery.util.Response;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface FoodDeliveryService {
    Response getFoodDeliveryOrdersByStationFoodStores(List<StationFoodStore> stationFoodStoreList, HttpHeaders headers);
}
