package com.example.trainfood.service;


import com.example.trainfood.domain.Food;
import com.example.trainfood.util.Response;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface TrainFoodService {
    Response<List<Food>> listTrainFoodByTripId(String tripId, HttpHeaders headers);
}
