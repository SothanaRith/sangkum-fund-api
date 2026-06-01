package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class EventCreateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private BigDecimal targetAmount;

    private LocalDate startDate;
    private LocalDate endDate;
    
    private String imageUrl;

    private String visibility; // PUBLIC / PRIVATE
    
    private String location;
    
    private Double latitude;
    
    private Double longitude;
    
    private String category;
    
    private String khqrImage;
    
    private String bakongAccountId;
}
