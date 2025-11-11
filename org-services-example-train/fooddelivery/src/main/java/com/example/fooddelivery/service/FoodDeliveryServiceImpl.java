package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.FoodDeliveryOrder;
import com.example.fooddelivery.domain.StationFoodStore;
import com.example.fooddelivery.repository.FoodDeliveryOrderRepository;
import com.example.fooddelivery.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FoodDeliveryServiceImpl implements FoodDeliveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoodDeliveryServiceImpl.class);

    @Autowired
    private FoodDeliveryOrderRepository foodDeliveryOrderRepository;

    @Override
    public Response getFoodDeliveryOrdersByStationFoodStores(List<StationFoodStore> stationFoodStoreList, HttpHeaders headers) {
        if (stationFoodStoreList == null || stationFoodStoreList.isEmpty()) {
            LOGGER.error("[getFoodDeliveryOrdersByStationFoodStores] StationFoodStore list is empty");
            return new Response<>(0, "StationFoodStore list is empty", null);
        }

        List<FoodDeliveryOrder> allDeliveryOrders = new ArrayList<>();

        for (StationFoodStore store : stationFoodStoreList) {
            String stationFoodStoreId = store.getId();
            Optional<FoodDeliveryOrder> deliveryOrderOptional = foodDeliveryOrderRepository.findByStationFoodStoreId(stationFoodStoreId);

            if (deliveryOrderOptional.isPresent()) {
                FoodDeliveryOrder deliveryOrder = deliveryOrderOptional.get();
                allDeliveryOrders.add(deliveryOrder);
                LOGGER.info("[getFoodDeliveryOrdersByStationFoodStores] Get food delivery order by storeId {} success", stationFoodStoreId);
            } else {
                LOGGER.warn("[getFoodDeliveryOrdersByStationFoodStores] No food delivery order found for storeId {}", stationFoodStoreId);
            }
        }

        return new Response<>(1, "Get success", allDeliveryOrders);
    }
}

