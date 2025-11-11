package com.example.travel2.service;

import com.example.travel2.domain.Trip;
import com.example.travel2.domain.TripId;
import com.example.travel2.repository.TripRepository;
import com.example.travel2.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class TravelServiceImpl {

    public static final Logger LOGGER = LoggerFactory.getLogger(TravelServiceImpl.class);

    @Autowired
    private TripRepository repository;

    public Response retrieve_food(String tripId, HttpHeaders headers) {
        TripId ti = new TripId(tripId);
        Trip trip = repository.findByTripId(ti);

        if (trip != null) {
            LOGGER.info("[retrieve][Trip found][TripId: {}]", tripId);
            // 将 trip 数据发送到 food 服务
            try {
                // 调用 sendToFoodService 方法发送 REST 请求
                boolean success = sendToFoodService(trip);
                if (success) {
                    return new Response<>(1, "Search Trip Success and sent to Food Service by Trip Id " + tripId, trip);
                } else {
                    return new Response<>(0, "Search Trip Success but failed to send to Food Service", trip);
                }
            } catch (Exception e) {
                LOGGER.error("[retrieve][Error while sending to Food Service][TripId: {}][Error: {}]", tripId, e.getMessage());
                return new Response<>(0, "Search Trip Success but failed to send to Food Service due to error", trip);
            }
        } else {
            LOGGER.error("[retrieve][Retrieve trip error][Trip not found][TripId: {}]", tripId);
            return new Response<>(0, "No Content according to tripId " + tripId, null);
        }
    }

    // 发送 Trip 数据到 Food 服务的方法
    private boolean sendToFoodService(Trip trip) {
        // REST 服务地址
        String foodServiceUrl = "http://localhost:8002/food-service/process-trip"; // 替换为 Food 服务的实际 URL

        // 创建 RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 设置 HTTP Headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 构造 HTTP 请求
        HttpEntity<Trip> requestEntity = new HttpEntity<>(trip, httpHeaders);

        // 发送 POST 请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(foodServiceUrl, requestEntity, String.class);

        // 检查响应状态
        return responseEntity.getStatusCode() == HttpStatus.OK;
    }
}
