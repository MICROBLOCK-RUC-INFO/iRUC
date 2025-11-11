package com.example.travel2.domain;

import lombok.Data;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Data
@Embeddable
public class TripId implements Serializable {

    @NotNull
    private String type;

    @NotNull
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
