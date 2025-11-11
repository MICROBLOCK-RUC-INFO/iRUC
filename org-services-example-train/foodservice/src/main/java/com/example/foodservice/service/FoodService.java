package com.example.foodservice.service;


import com.example.foodservice.domain.Trip;
import com.example.foodservice.util.Response;
import org.springframework.http.HttpHeaders;

public interface FoodService {
    Response getAllFood(Trip trip, HttpHeaders headers);
}
