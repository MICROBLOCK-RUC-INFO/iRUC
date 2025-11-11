package com.example.fooddelivery.domain;

import lombok.Data;

import java.util.List;

@Data

public class StationFoodStore {

    private String id;

    private String stationName;

    private String storeName;

    private String telephone;

    private String businessTime;

    private double deliveryFee;

    private List<Food> foodList;

    public StationFoodStore() {
        // Default Constructor
        this.stationName = "";
    }
}
