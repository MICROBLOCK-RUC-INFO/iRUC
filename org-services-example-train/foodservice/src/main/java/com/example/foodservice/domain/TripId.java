package com.example.foodservice.domain;

import lombok.Data;

@Data
public class TripId {

    private String type;

    private String number;

    public TripId() {
        // 默认构造函数
    }

    public TripId(String id) {
        String[] parts = id.split("-");
        this.type = parts[0];
        this.number = parts.length > 1 ? parts[1] : "";
    }

    @Override
    public String toString() {
        return type + "-" + number;
    }
}
