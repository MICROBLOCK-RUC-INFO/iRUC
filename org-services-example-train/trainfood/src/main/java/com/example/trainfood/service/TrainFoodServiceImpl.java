package com.example.trainfood.service;


import ch.qos.logback.classic.Logger;
import com.example.trainfood.domain.Food;
import com.example.trainfood.domain.TrainFood;
import com.example.trainfood.repository.TrainFoodRepository;
import com.example.trainfood.util.Response;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
public class TrainFoodServiceImpl implements TrainFoodService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TrainFoodServiceImpl.class);
    @Autowired
    private TrainFoodRepository trainFoodRepository;

    @Override
    public Response listTrainFoodByTripId(String tripId, HttpHeaders headers) {
        TrainFoodServiceImpl.LOGGER.info("[listTrainFoodByTripId][Query train food list by trip id][tripId: {}]", tripId);

        List<Food> trainFoodList = null;
        TrainFood trainFood = trainFoodRepository.findByTripId(tripId);

        if (trainFood != null && trainFood.getFoodList() != null && !trainFood.getFoodList().isEmpty()) {
            trainFoodList = trainFood.getFoodList();
            TrainFoodServiceImpl.LOGGER.info("[listTrainFoodByTripId][Query train food list success][tripId: {}, size: {}]", tripId, trainFoodList.size());
            return new Response<>(1, "Success", trainFoodList);
        } else {
            TrainFoodServiceImpl.LOGGER.error("[listTrainFoodByTripId][List train food error][tripId: {}]: {}", tripId, "No Content");
            return new Response<>(0, "No Content", null);
        }
    }

}
