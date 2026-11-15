package com.rideshare.locationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class NearByDriverResponse {
    private String driverId;
    private double longitude;
    private double latitude;
    private double distanceInKms;
}
