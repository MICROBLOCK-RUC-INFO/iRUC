package com.example.travel2.repository;

import com.example.travel2.domain.Trip;
import com.example.travel2.domain.TripId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TripRepository extends JpaRepository<Trip, String> {
    Trip findByTripId(TripId tripId);
}
