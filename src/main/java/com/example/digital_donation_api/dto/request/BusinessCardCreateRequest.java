package com.example.digital_donation_api.dto.request;

import lombok.Data;

@Data
public class BusinessCardCreateRequest {
    private String template;
    private String title;
    private String bio;
    private String contactInfo; // JSON string
}
