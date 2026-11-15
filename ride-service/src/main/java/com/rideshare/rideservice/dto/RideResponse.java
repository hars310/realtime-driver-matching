package com.rideshare.rideservice.dto;

import java.time.LocalDateTime;

import com.rideshare.rideservice.model.RideStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideResponse {

    private String rideId;

    private String riderId;

    private String driverId;

    private double pickupLatitude;

    private double pickupLongitude;

    private String pickupAddress;

    private double dropLatitude;

    private double dropLongitude;

    private String dropAddress;

    // to track lifecycle of the ride
    private RideStatus status;

    private double estimatedFare;
    private double actualFare;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime rideStartedAt;
    private LocalDateTime rideCompletedAt;
}
