package com.example.stationfood.controller;

import com.example.stationfood.domain.StationFoodStore;
import com.example.stationfood.service.StationFoodService;
import com.example.stationfood.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stationfood-service")
public class StationFoodController {

    @Autowired
    private StationFoodService stationFoodService;

    @PostMapping("/stationfoodstores")
    public ResponseEntity<Response<List<StationFoodStore>>> getFoodStoresByStationNames(
            @RequestBody List<String> stationNames, @RequestHeader HttpHeaders headers) {
        Response<List<StationFoodStore>> response = stationFoodService.getFoodStoresByStationNames(stationNames);
        return ResponseEntity.ok(response);
    }
}
