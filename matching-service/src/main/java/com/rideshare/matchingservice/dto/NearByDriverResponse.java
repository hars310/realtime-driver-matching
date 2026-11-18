package com.rideshare.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * NearByDriverResponse
 * Response recieved from location serivce
 * when queryingn for nearby drivers
 */

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class NearByDriverResponse {
    private String driverId;
    private double longitude;
    private double latitude;
    private double distanceInKms;
}
