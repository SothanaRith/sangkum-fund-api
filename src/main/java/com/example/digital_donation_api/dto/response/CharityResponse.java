package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CharityResponse {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String registrationNumber;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String category;
    private String status;
    private LocalDateTime verifiedAt;
}
