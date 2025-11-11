package com.example.stationfood.service;



import com.example.stationfood.domain.StationFoodStore;
import com.example.stationfood.util.Response;

import java.util.List;

public interface StationFoodService {

    Response<List<StationFoodStore>> getFoodStoresByStationNames(List<String> stationNames);
}
