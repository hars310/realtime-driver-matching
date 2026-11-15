package com.rideshare.locationservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    // REDIS key for all driver location
    private static final String DRIVERS_GEO_KEY = "drivers:locations";
    private static final long DRIVER_COUNTS_LIMIT = 10;

    /*
     * update drivrer location in redis
     * called every 3 second by driver
     * ---
     * Maps to redis GEOADD command
     */
    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating location for Driver: {}", driverLocationRequest.getDriverId());

        // important thing: ALWAYS PUT LONGITUDE FIRST AND LATITUDE SECOND
        Point driverPoint = new Point(driverLocationRequest.getLongitude(), driverLocationRequest.getLatitude());

        // opsForGeo() provides access to geospatial commands in java
        redisTemplate.opsForGeo().add(DRIVERS_GEO_KEY, driverPoint, driverLocationRequest.getDriverId());

        log.info("Location updated for Driver: {}", driverLocationRequest.getDriverId());
    }

    /*
     * Find nearby drivers withn=in given radius
     * called by matching service on ride request
     * RADIUS command for search
     */
    public List<NearByDriverResponse> findNearByDrivers(double longitude, double latitude, double radiusInKms) {
      
        log.info("finding nearby drivers at long: {} lat:{} within {}kms radius ", longitude, latitude, radiusInKms);

        // Create a circular search area around the customer's location.
        // Only drivers inside this circle will be returned.
        Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(radiusInKms, Metrics.KILOMETERS));

        // Search Redis GEO for drivers within the given circular area.
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(DRIVERS_GEO_KEY,
                searchArea, RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates() // each driver's latitude and longitude.
                        .includeDistance() // each driver's distance from the customer.
                        .sortAscending()
                        .limit(DRIVER_COUNTS_LIMIT)); 

        List<NearByDriverResponse> nearByDrivers = new ArrayList<>();

        
        if (results != null) {
            // check all the drivers returned by Redis.
            results.getContent().forEach(result -> {
                
                RedisGeoCommands.GeoLocation<String> location = result.getContent();

                // add the result to the nearbydrivers list
                nearByDrivers.add(new NearByDriverResponse(location.getName(), location.getPoint().getY(),
                        location.getPoint().getX(), result.getDistance().getValue()));
            });
        }

        log.info("Found {} drivers nearby", nearByDrivers.size());

        return nearByDrivers;
    }


    /*
    Remove driver when they go offline
    maps to REDIS ZREM command
    */
    public void removeDriver(String driverID) {
        log.info("Removing driver with ID: {}", driverID);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverID);
        log.info("Driver with ID: {} removed", driverID);
   }
}
