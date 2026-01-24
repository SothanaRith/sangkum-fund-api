package com.example.digital_donation_api.dto.response;

import lombok.Data;

@Data
public class BusinessCardResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String template;
    private String title;
    private String bio;
    private String contactInfo; // JSON string
    private String shareSlug;
    private String shareUrl;
}
