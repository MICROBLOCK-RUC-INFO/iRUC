package com.example.foodservice.controller;


import com.example.foodservice.domain.Trip;
import com.example.foodservice.service.FoodService;
import com.example.foodservice.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/food-service")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @PostMapping("/process-trip")
    public ResponseEntity<Response> processTrip(@RequestBody Trip trip, @RequestHeader HttpHeaders headers) {
        return ResponseEntity.ok(foodService.getAllFood(trip, headers));
    }
}
