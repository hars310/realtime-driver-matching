package com.rideshare.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class RideRequest {

    @NotBlank(message = "Rider id is required")
    private String riderId;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    @NotBlank(message = "Pickup Address is required")
    private String pickupAddress;

    @NotNull(message = "Drop latitude is required")
    private Double dropLatitude;

    @NotNull(message = "Drop longitude is required")
    private Double dropLongitude;

    @NotBlank(message = "Drop Address is required")
    private String dropAddress;
}