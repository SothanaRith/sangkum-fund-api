package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String action;
    private String details;
    private String type; // "EVENT_CREATED", "DONATION_RECEIVED", "USER_JOINED", etc.
    private LocalDateTime createdAt;
    private String userName;
    private String userEmail;
}
