package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipantResponse {
    private Long userId;
    private String userName;
    private String userAvatar;
    private LocalDateTime joinedAt;
    private BigDecimal totalDonated;
    private Integer donationCount;
}
