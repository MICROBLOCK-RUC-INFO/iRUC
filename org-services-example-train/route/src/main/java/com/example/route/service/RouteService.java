package com.example.route.service;


import com.example.route.domain.Route;
import com.example.route.domain.Trip;
import com.example.route.util.Response;
import org.springframework.http.HttpHeaders;

public interface RouteService {
    Response<Route> getRouteByTrip(Trip trip, HttpHeaders headers);
}
