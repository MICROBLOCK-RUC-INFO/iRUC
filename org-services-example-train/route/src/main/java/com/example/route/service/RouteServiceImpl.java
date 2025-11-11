package com.example.route.service;


import com.example.route.domain.Route;
import com.example.route.domain.StationFoodStore;
import com.example.route.domain.Trip;
import com.example.route.repository.RouteRepository;
import com.example.route.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServiceImpl.class);

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Response<Route> getRouteByTrip(Trip trip, HttpHeaders headers) {
        String routeId = trip.getRouteId();
        Optional<Route> routeOptional = routeRepository.findById(routeId);

        if (!routeOptional.isPresent()) {
            LOGGER.error("[getRouteByTrip][Find route error][Route not found][RouteId: {}]", routeId);
            return new Response<>(0, "No content with the routeId", null);
        }

        Route route = routeOptional.get();
        LOGGER.info("[getRouteByTrip][Route found][RouteId: {}]", routeId);

        // 调用生成 stations 的方法
        //System.out.println(route.getStations());
        List<String> stations = generateStations(route.getStations(), trip.getStartStationName(), trip.getTerminalStationName());
        //System.out.println(stations);
        LOGGER.info("[getRouteByTrip][Filtered stations][Stations: {}]", stations);


        // 调用 stationfood 服务
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(stations, headers);
        String stationFoodServiceUrl = "http://localhost:8005/stationfood-service/stationfoodstores";

        try {
            ResponseEntity<Response<List<StationFoodStore>>> responseEntity = restTemplate.exchange(
                    stationFoodServiceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Response<List<StationFoodStore>>>() {
                    });

            List<StationFoodStore> foodStores = responseEntity.getBody().getData();
            LOGGER.info("[getRouteByTrip][StationFoodService response][FoodStores: {}]", foodStores);

        } catch (Exception e) {
            LOGGER.error("[getRouteByTrip][StationFoodService call failed][Error: {}]", e.getMessage());
        }



        // 返回原始的 route
        return new Response<>(1, "Success", route);
    }

    /**
     * 生成过滤后的站点列表
     *
     * @param stations        原始站点列表
     * @param startStation    起点站
     * @param endStation      终点站
     * @return 过滤后的站点列表
     */
    private List<String> generateStations(List<String> stations, String startStation, String endStation) {
        List<String> filteredStations = new ArrayList<>(stations);

        if (startStation != null && !startStation.isEmpty()) {
            for (int i = 0; i < filteredStations.size(); i++) {
                if (filteredStations.get(i).equals(startStation)) {
                    break;
                } else {
                    filteredStations.remove(i);
                    i--; // 确保下标调整
                }
            }
        }

        if (endStation != null && !endStation.isEmpty()) {
            for (int i = filteredStations.size() - 1; i >= 0; i--) {
                if (filteredStations.get(i).equals(endStation)) {
                    break;
                } else {
                    filteredStations.remove(i);
                }
            }
        }

        return filteredStations;
    }
}
