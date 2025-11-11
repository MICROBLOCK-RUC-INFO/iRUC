package com.example.foodservice.service;

import com.example.foodservice.domain.Food;
import com.example.foodservice.domain.FoodOrder;
import com.example.foodservice.domain.Route;
import com.example.foodservice.domain.Trip;
import com.example.foodservice.repository.FoodOrderRepository;
import com.example.foodservice.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FoodServiceImpl implements FoodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoodServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Override
    public Response getAllFood(Trip trip, HttpHeaders headers) {
        LOGGER.info("[getAllFood][Processing trip][trip: {}]", trip);

        // 从 Trip 中提取 train_type_name
        String trainTypeName = trip.getTrainTypeName();

        if (null == trainTypeName || trainTypeName.length() == 0) {
            LOGGER.error("[getAllFood][Get the Get Food Request Failed][Train type name is not suitable][trip: {}]", trip);
            return new Response<>(0, "Train type name is not suitable", null);
        }

        // need return these elements
        FoodOrder orderResult = null;
        List<Food> trainFoodList = null;
        Route route = null;

        /**--------------------------------------------------------------------------------------*/
        // 调用 findByOrderId 查询本地数据库
        FoodOrder fo = foodOrderRepository.findByOrderId(trainTypeName);
        if (fo != null) {
            orderResult = fo;
            LOGGER.info("[getAllFood][Find Order by id Success][trainTypeName: {}]", trainTypeName);
        } else {
            LOGGER.error("[getAllFood][Order not found][trainTypeName: {}]", trainTypeName);
            return new Response<>(0, "Order not found", null);
        }

        /**--------------------------------------------------------------------------------------*/
        // 调用 trainfood 服务
        HttpEntity requestEntityGetTrainFoodListResult = new HttpEntity(trip, headers);
        String train_food_service_url = "http://localhost:8003/trainfood-service/trainfoods";
        ResponseEntity<Response<List<Food>>> reGetTrainFoodListResult = restTemplate.exchange(
                train_food_service_url,
                HttpMethod.POST,
                requestEntityGetTrainFoodListResult,
                new ParameterizedTypeReference<Response<List<Food>>>() {
                });

        List<Food> trainFoodListResult = reGetTrainFoodListResult.getBody().getData();

        if (trainFoodListResult != null) {
            trainFoodList = trainFoodListResult;
            LOGGER.info("[getAllFood][Get Train Food List Success!]");
        } else {
            LOGGER.error("[getAllFood][reGetTrainFoodListResult][Train Food List fetch failed][trip: {}]", trip);
            return new Response<>(0, "Failed to fetch Train Food List", null);
        }

        /**--------------------------------------------------------------------------------------*/
        // 调用 route 服务
        HttpEntity requestEntityGetRouteResult = new HttpEntity(trip, headers);
        String route_service_url = "http://localhost:8004/route-service/routes";
        ResponseEntity<Response<Route>> reGetRouteResult = restTemplate.exchange(
                route_service_url,
                HttpMethod.POST,
                requestEntityGetRouteResult,
                new ParameterizedTypeReference<Response<Route>>() {
                });
        Response<Route> routeResponse = reGetRouteResult.getBody();

        if (routeResponse.getStatus() == 1) {
            route = routeResponse.getData();
            LOGGER.info("[getAllFood][Get Route Success!]");
        } else {
            LOGGER.error("[getAllFood][reGetRouteResult][Route fetch failed][trip: {}]", trip);
            return new Response<>(0, "Failed to fetch Route", null);
        }

        /**--------------------------------------------------------------------------------------*/
        // 返回结果整合
        Map<String, Object> result = new HashMap<>();
        result.put("order", orderResult);
        result.put("trainFoodList", trainFoodList);
        result.put("route", route);

        // 查看列表
        System.out.println("------------------------------------");
        System.out.println(trainFoodList);
        System.out.println(route);

        return new Response<>(1, "Get All Food Success", result);
    }
}
