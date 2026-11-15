package com.rideshare.rideservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    Event published to kafkka when a ride is requested 
    Matching  serivce consumes this event
    Event: ride.requested
*/
@Data
@AllArgsConstructor
@NoArgsConstructor 
public class RideRequestedEvent {
    private String rideId;
    private String riderId;
    private String driverId;

    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;

    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;

}
