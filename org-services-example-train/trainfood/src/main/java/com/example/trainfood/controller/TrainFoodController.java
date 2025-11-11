package com.example.trainfood.controller;


import com.example.trainfood.domain.Food;
import com.example.trainfood.domain.Trip;
import com.example.trainfood.service.TrainFoodService;
import com.example.trainfood.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/trainfood-service")
public class TrainFoodController {

    @Autowired
    private TrainFoodService trainFoodService;

    @PostMapping("/trainfoods")
    public ResponseEntity<Response<List<Food>>> getTrainFoods(@RequestBody Trip trip, @RequestHeader HttpHeaders headers) {
        // 调用 Service 层获取食品列表
        String tripId = trip.getTripId().toString();
        Response<List<Food>> response = trainFoodService.listTrainFoodByTripId(tripId, headers);
        return ResponseEntity.ok(response);
    }
}
