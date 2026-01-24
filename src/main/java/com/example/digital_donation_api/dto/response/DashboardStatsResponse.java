package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class DashboardStatsResponse {
    private BigDecimal totalDonations;
    private Integer totalEvents;
    private Integer totalUsers;
}
