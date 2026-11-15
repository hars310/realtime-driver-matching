
package com.rideshare.rideservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
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

    // Create a new ride
    public RideResponse rideRequested(RideRequest rideRequest) {

        log.info("Creating ride for riderId: {}", rideRequest.getRiderId());

        Ride ride = new Ride();

        ride.setRiderId(rideRequest.getRiderId());
        ride.setPickupLatitude(rideRequest.getPickupLatitude());
        ride.setPickupLongitude(rideRequest.getPickupLongitude());
        ride.setPickupAddress(rideRequest.getPickupAddress());
        ride.setDropLatitude(rideRequest.getDropLatitude());
        ride.setDropLongitude(rideRequest.getDropLongitude());
        ride.setDropAddress(rideRequest.getDropAddress());

        ride.setStatus(RideStatus.REQUESTED);

        Ride savedRide = rideRepository.save(ride);

        log.info("Ride created successfully with rideId: {}", savedRide.getRideId());

        return mapToRideResponse(savedRide);
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
    @Transactional
    public RideResponse startRide(String rideId) {

        log.info("Starting ride with rideId: {}", rideId);

        Ride ride = getRide(rideId);

        if (ride.getStatus() != RideStatus.DRIVER_ARRIVING) {
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
    @Transactional
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

        Ride updatedRide = rideRepository.save(ride);

        log.info("Ride completed successfully: {}", rideId);

        return mapToRideResponse(updatedRide);
    }

    // Cancel ride
    @Transactional
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

        response.setId(ride.getRideId());
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
}
