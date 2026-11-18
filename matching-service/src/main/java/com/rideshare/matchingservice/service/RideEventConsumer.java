package com.rideshare.matchingservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.rideshare.matchingservice.event.RideRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j 
public class RideEventConsumer {
    private final MatchingService matchingService;

    // listens to kafka topic ride.requested
    // triggers everytime ride  service  publishes a newride request

    // flow
    // ride service -> Kafka(ride.requested) -> this consumer - > Matching service

    @KafkaListener(
        topics = "ride.requested",
        groupId=  "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent rideRequestedEvent) {
        try {
            matchingService.matchDriverForRide(rideRequestedEvent);
        } catch (Exception e) {
            log.error("Error processing ride request: {} - {}",rideRequestedEvent.getRideId(),e.getMessage());
        }
    }
}
