package com.rideshare.locationservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import com.rideshare.locationservice.service.LocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("api/v1/locations")
@Slf4j
@RequiredArgsConstructor 
public class LocationController {
    
    private final LocationService locationService;

    // method1: get driver location at every 3 second updates it with their Id
    // driver phone sending this api call every 3 second
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(@RequestBody DriverLocationRequest driverLocationRequest){
        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver location updated");
    }

    // method2: get nearby drivers
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearByDrivers(@RequestParam double longitude, @RequestParam double latitude, @RequestParam (defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(locationService.findNearByDrivers(longitude,latitude,radius));
    }
    

    // method3: when driver goes offline
    @DeleteMapping("/drivers/{driverID}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverID) {
        locationService.removeDriver(driverID);
        return ResponseEntity.ok("Driver removed successfully");
    }
    
}
