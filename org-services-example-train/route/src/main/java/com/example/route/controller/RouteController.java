package com.example.route.controller;


import com.example.route.domain.Route;
import com.example.route.domain.Trip;
import com.example.route.service.RouteService;
import com.example.route.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route-service")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/routes")
    public ResponseEntity<Response<Route>> getRoute(@RequestBody Trip trip, @RequestHeader HttpHeaders headers) {
        // 调用服务获取 Route 数据
        Response<Route> response = routeService.getRouteByTrip(trip, headers);
        return ResponseEntity.ok(response);
    }
}
