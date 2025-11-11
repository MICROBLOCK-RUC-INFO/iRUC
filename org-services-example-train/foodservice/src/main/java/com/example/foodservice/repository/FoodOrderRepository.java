package com.example.foodservice.repository;


import com.example.foodservice.domain.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, String> {

    // 根据 orderId 查询 FoodOrder
    FoodOrder findByOrderId(String orderId);
}
