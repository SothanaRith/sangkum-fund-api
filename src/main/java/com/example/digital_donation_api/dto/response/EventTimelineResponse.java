package com.example.digital_donation_api.dto.response;

import com.example.digital_donation_api.entity.TimelineType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventTimelineResponse {
    private Long id;
    private Long eventId;
    private TimelineType type;
    private String description;
    private LocalDateTime createdAt;
}
