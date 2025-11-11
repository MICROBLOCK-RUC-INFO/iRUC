package com.example.fooddelivery.controller;


import com.example.fooddelivery.domain.StationFoodStore;
import com.example.fooddelivery.service.FoodDeliveryService;
import com.example.fooddelivery.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fooddelivery-service")
public class FoodDeliveryController {

    @Autowired
    private FoodDeliveryService foodDeliveryService;

    @PostMapping("/handlefoodstores")
    public ResponseEntity<Response<List<StationFoodStore>>> handleFoodStores(
            @RequestBody List<StationFoodStore> stationFoodStoreList) {
        Response<List<StationFoodStore>> response = foodDeliveryService.handleFoodStores(stationFoodStoreList);
        return ResponseEntity.ok(response);
    }
}
