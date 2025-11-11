package com.example.fooddelivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;


import java.io.Serializable;

@Data
@Embeddable
public class Food implements Serializable {

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "price", nullable = false)
    private double price;

    public Food() {
        // Default Constructor
    }

    public Food(String foodName, double price) {
        this.foodName = foodName;
        this.price = price;
    }
}
