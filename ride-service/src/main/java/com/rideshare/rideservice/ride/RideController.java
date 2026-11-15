package com.rideshare.rideservice.ride;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.service.RideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor 
public class RideController {
    private final RideService rideService;

    // rider requests a new Ride
    @PostMapping("/request")
    public ResponseEntity<RideResponse> rideRequested(@Valid @RequestBody RideRequest rideRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.rideRequested(rideRequest));
    }
    
    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable  String rideId) {
        return ResponseEntity.ok(rideService.getRideById(rideId));
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<Ride>> getRidesByRider(@PathVariable String riderId) {
        return ResponseEntity.ok(rideService.getRidesByRider(riderId));
    }

    @PutMapping("{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable String rideId) {

        return ResponseEntity.ok(rideService.startRide(rideId));
    }
    
    @PutMapping("{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId) {

        return ResponseEntity.ok(rideService.completeRide(rideId));
    }
    
    @PutMapping("{rideId}/cancelled")
    public ResponseEntity<RideResponse> cancelledRide(@PathVariable String rideId) {

        return ResponseEntity.ok(rideService.canclledRide(rideId));
    }
    
}
