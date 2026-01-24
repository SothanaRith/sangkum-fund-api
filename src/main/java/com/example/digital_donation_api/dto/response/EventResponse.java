package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private String status;
    private String visibility;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
    private Long charityId;
    private String charityName;
    private String charityLogo;
    private Integer donationCount;
    private Integer participantCount;
    private Double progressPercentage;
}
