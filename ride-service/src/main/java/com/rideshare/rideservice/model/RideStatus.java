package com.rideshare.rideservice.model;


/*
FLOW

1-> REQUESTED
2-> MATCHING
3-> ACCEPTED
4-> DRIVER_ARRIVING
5-> RIDE_STARTED
6-> COMPLETED
7-> CANCELLED(can happen at diff stages)
*/
public enum RideStatus {
    REQUESTED,
    MATCHING,
    ACCEPTED,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED
}
