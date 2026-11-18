package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.dto.NearByDriverResponse;
import org.apache.kafka.common.internals.Topic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.rideshare.matchingservice.client.LocationServiceClient;
import com.rideshare.matchingservice.event.RideMatchedEvent;
import com.rideshare.matchingservice.event.RideRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor 
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;
    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * 
     * most important part of entire code
     * called when RideRequestedEvent   is consumed by kafka
     * 
     * Step1: ask location service for nearby drivers
     */
    public void matchDriverForRide(RideRequestedEvent event)
    {
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM);

        if (nearByDrivers.isEmpty()) {
            log.info("No nearby drivers were found.");
            return;
        }
        //STEP2:
        // score to get the apporpriate driver for the rider
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);
        if (bestDriver.isEmpty()) {
            log.info("Could not find suitable driver for your ride");
            return;
        }
        NearByDriverResponse assignedDriver = bestDriver.get();

        //STEP3: publish a kafka event "ride.matched"
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKms());

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), matchedEvent);
        log.info("Ride matched event published in kafka");
    }
    
    // Driver scoring algo to find best driver
    // to optimise and make it more efficient there can be factors like driver rating, acceptance rate, behaviour, riding experience, traffic rules, etc
    // but i am keeping this algo juston the basis of driver location and rating
    private Optional<NearByDriverResponse> findBestDriver(List<NearByDriverResponse> nearByDrivers) {
        double distanceProb = 0.7;
        double ratingProb = 0.3;

        return nearByDrivers.stream().max(
            Comparator.comparingDouble(
                driver ->
                {
                    double distanceScore = (1/(driver.getDistanceInKms() +0.1))*distanceProb;
                    
                    
                    // i am getting a random rating for all drivers from a rng
                    double rating = 4.0 + Math.random();
                    double ratingScore = ratingProb * rating;
                    
                    return distanceScore + ratingScore;
                }
            )
        );
    }
}
