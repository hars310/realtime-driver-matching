package com.rideshare.matchingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * RideMatchedEvent
 * event published to kafka topic: ride.matched
 * and consumed by ride service to update 
 * with assigned driver
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideMatchedEvent {
    private String rideId;
    private String riderId;
    private String driverId;
    private double driverLatitude;
    private double driverLongitude;
    private double distanceToPickupInKms;
}
