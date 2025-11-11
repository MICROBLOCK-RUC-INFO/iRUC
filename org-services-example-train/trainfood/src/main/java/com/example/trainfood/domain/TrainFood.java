package com.example.trainfood.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;


@Data
@Entity
@GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainFood {

    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 36)
    private String id;

    @NotNull
    @Column(name = "trip_id", unique = true) // 修复映射
    private String tripId;

    @ElementCollection(targetClass = Food.class)
    @CollectionTable(
            name = "train_food_list",
            joinColumns = @JoinColumn(name = "trip_id", referencedColumnName = "trip_id") // 正确引用主表的 trip_id
    )
    private List<Food> foodList;

    public TrainFood() {
        // Default Constructor
        this.tripId = "";
    }
}

