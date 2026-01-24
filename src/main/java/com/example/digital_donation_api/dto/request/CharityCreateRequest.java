package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CharityCreateRequest {

    @NotBlank
    private String name;

    private String description;
    
    private String logo;
    
    @NotBlank
    private String registrationNumber;
    
    private String address;
    
    private String contactEmail;
    
    private String contactPhone;
    
    private String website;
}
