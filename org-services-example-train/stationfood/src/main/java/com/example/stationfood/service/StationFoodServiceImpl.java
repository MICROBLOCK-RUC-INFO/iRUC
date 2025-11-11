package com.example.stationfood.service;


import com.example.stationfood.domain.StationFoodStore;
import com.example.stationfood.repository.StationFoodStoreRepository;
import com.example.stationfood.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class StationFoodServiceImpl implements StationFoodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationFoodServiceImpl.class);

    @Autowired
    private StationFoodStoreRepository stationFoodStoreRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Response<List<StationFoodStore>> getFoodStoresByStationNames(List<String> stationNames) {
        List<StationFoodStore> stationFoodStoreList = stationFoodStoreRepository.findByStationNameIn(stationNames);

        if (stationFoodStoreList != null && !stationFoodStoreList.isEmpty()) {
            LOGGER.info("[getFoodStoresByStationNames][Food stores found][StationNames: {}]", stationNames);

            // 调用 fooddelivery 服务
            String foodDeliveryServiceUrl = "http://localhost:8006/fooddelivery-service/handlefoodstores";
            HttpEntity<List<StationFoodStore>> requestEntity = new HttpEntity<>(stationFoodStoreList);

            try {
                ResponseEntity<Response<List<StationFoodStore>>> responseEntity = restTemplate.exchange(
                        foodDeliveryServiceUrl,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<Response<List<StationFoodStore>>>() {}
                );

                List<StationFoodStore> updatedFoodStoreList = responseEntity.getBody().getData();
                LOGGER.info("[getFoodStoresByStationNames][FoodDelivery response][UpdatedFoodStores: {}]", updatedFoodStoreList);

                return new Response<>(1, "Success", updatedFoodStoreList);

            } catch (Exception e) {
                LOGGER.error("[getFoodStoresByStationNames][FoodDelivery call failed][Error: {}]", e.getMessage());
                return new Response<>(0, "Failed to call FoodDeliveryService", null);
            }

        } else {
            LOGGER.error("[getFoodStoresByStationNames][No food stores found][StationNames: {}]", stationNames);
            return new Response<>(0, "No content for the provided station names", null);
        }
    }
}
