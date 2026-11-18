package com.rideshare.rideservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.rideshare.rideservice.event.RideMatchedEvent;
import com.rideshare.rideservice.service.RideService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideMatchedEventConsumer {
    private final RideService rideService;

    // listens to kafka topic ride.matched
    // triggers everytime matching service finds a driver for a ride

    // flow
    // matching service -> Kafka(ride.matched) -> this consumer -> Ride service

    @KafkaListener(
        topics = "ride.matched",
        groupId = "ride-service-group"
    )
    public void consumeRideMatchedEvent(RideMatchedEvent event) {
        try {
            rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
        } catch (Exception e) {
            log.error("Error processing ride matched event: {} - {}", event.getRideId(), e.getMessage());
        }
    }
}
