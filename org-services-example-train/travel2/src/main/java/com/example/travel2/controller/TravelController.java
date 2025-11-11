package com.example.travel2.controller;

import com.example.travel2.service.TravelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.travel2.util.Response;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    private TravelServiceImpl travelService;

    @GetMapping("/retrieve_food/{tripId}")
    public ResponseEntity<Response> retrieveFood(@PathVariable String tripId, @RequestHeader HttpHeaders headers) {
        Response response = travelService.retrieve_food(tripId, headers);
        return ResponseEntity.ok(response);
    }
}
