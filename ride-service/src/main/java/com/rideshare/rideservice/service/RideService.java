
package com.rideshare.rideservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    /*
     * step1: Create a new ride
     * step2: publish the even to kafka
     */
    public RideResponse rideRequested(RideRequest rideRequest) {

        log.info("Creating ride for riderId: {}", rideRequest.getRiderId());

        //step1:
        Ride ride = new Ride();

        ride.setRiderId(rideRequest.getRiderId());
        ride.setPickupLatitude(rideRequest.getPickupLatitude());
        ride.setPickupLongitude(rideRequest.getPickupLongitude());
        ride.setPickupAddress(rideRequest.getPickupAddress());
        ride.setDropLatitude(rideRequest.getDropLatitude());
        ride.setDropLongitude(rideRequest.getDropLongitude());
        ride.setDropAddress(rideRequest.getDropAddress());
        ride.setEstimatedFare(calculateEstimatedFare(rideRequest)); 
        ride.setStatus(RideStatus.REQUESTED);
        Ride savedRide = rideRepository.save(ride);




        // step2:
        // matching service will consume this event and will find the nearest driver

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getRideId(),
                savedRide.getRiderId(),
                savedRide.getDriverId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getRideId(), event);
        log.info("RideRequestEvent published to kafka for ride: {}", savedRide.getRideId());

        log.info("Ride created successfully with rideId: {}", savedRide.getRideId());
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);
        return mapToRideResponse(savedRide);
    }

    // this mthod is used by matching service to when a driver is found
    // and updates the ride status to accepeted
    // and assigns the driver
    public void updateRideWithDriver(String rideId, String driverId) {
        Ride ride = getRide(rideId);


        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    // Get ride by ID
    public RideResponse getRideById(String rideId) {

        log.info("Fetching ride with rideId: {}", rideId);

        Ride ride = getRide(rideId);

        return mapToRideResponse(ride);
    }

    // Get all rides for a rider
    public List<Ride> getRidesByRider(String riderId) {

        log.info("Fetching rides for riderId: {}", riderId);

        return rideRepository.findByRiderId(riderId);
    }

    // Start ride
    public RideResponse startRide(String rideId) {

        log.info("Starting ride with rideId: {}", rideId);

        Ride ride = getRide(rideId);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ride can only be started when driver is arriving");
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setRideStartedAt(LocalDateTime.now());

        Ride updatedRide = rideRepository.save(ride);

        log.info("Ride started successfully: {}", rideId);

        return mapToRideResponse(updatedRide);
    }

    // Complete ride
    public RideResponse completeRide(String rideId) {

        log.info("Completing ride with rideId: {}", rideId);

        Ride ride = getRide(rideId);

        if (ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only started rides can be completed");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setRideCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());

        Ride updatedRide = rideRepository.save(ride);

        log.info("Ride completed successfully: {}", rideId);

        return mapToRideResponse(updatedRide);
    }

    // Cancel ride
    public RideResponse canclledRide(String rideId) {

        log.info("Cancelling ride with rideId: {}", rideId);

        Ride ride = getRide(rideId);

        if (ride.getStatus() == RideStatus.COMPLETED
                || ride.getStatus() == RideStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ride cannot be cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);

        Ride updatedRide = rideRepository.save(ride);

        log.info("Ride cancelled successfully: {}", rideId);

        return mapToRideResponse(updatedRide);
    }

    // Common method to fetch ride
    private Ride getRide(String rideId) {

        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found"));
    }

    // Convert Ride entity to RideResponse DTO
    private RideResponse mapToRideResponse(Ride ride) {

        RideResponse response = new RideResponse();

        response.setRideId(ride.getRideId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());

        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());

        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());

        response.setStatus(ride.getStatus());

        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());

        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());

        response.setRideStartedAt(ride.getRideStartedAt());
        response.setRideCompletedAt(ride.getRideCompletedAt());

        return response;
    }

    // calculate estimated fare
    public double calculateEstimatedFare(RideRequest rideRequest) {

        double distance = calculateDistance(
                rideRequest.getPickupLatitude(),
                rideRequest.getPickupLongitude(),
                rideRequest.getDropLatitude(),
                rideRequest.getDropLongitude());

        double baseFare = 50.0;
        double perKmRate = 12.0;

        return baseFare + (distance * perKmRate);
    }

    
    // Calculates the straight-line distance between two geographical coordinates
    // using the Haversine formula.
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        // Average radius of the Earth in kilometres.
        final double EARTH_RADIUS = 6371;

        // Calculate the difference between the two latitudes and convert it from
        // degrees to radians.
        double latDistance = Math.toRadians(lat2 - lat1);

        // Calculate the difference between the two longitudes and convert it from
        // degrees to radians.
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula:
        // Calculates the square of half the chord distance between the two geographical
        // points.
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        // Calculates the angular distance between the two points in radians.
        // atan2 is used to calculate the angle accurately.
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Multiply the angular distance by Earth's radius to get the actual distance in
        // kilometres.
        return EARTH_RADIUS * c;
    }
}
