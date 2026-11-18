package com.rideshare.rideservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    Event published to kafka when a driver is matched for a ride
    Matching service publishes this event, ride service consumes it
    Event: ride.matched
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
