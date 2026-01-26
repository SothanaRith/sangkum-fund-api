package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CharityStatsResponse {
    private Long charityId;
    private String charityName;
    private Long totalDonations;
    private Long beneficiariesCount;
    private Long volunteersCount;
    private Integer yearsActive;
    private Long totalEvents;
    private Long activeEventsCount;
    private Long totalDonorsCount;
    private Long totalRaisedAmount;
    private Double averageRating;
    private Integer reviewCount;
    private Long activeProjects;
    private String impactPercentage; // % of goal achieved
}
