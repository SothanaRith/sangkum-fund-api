package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.EventTimelineResponse;
import com.example.digital_donation_api.entity.EventTimeline;

public class EventTimelineMapper {

    public static EventTimelineResponse toResponse(EventTimeline timeline) {
        EventTimelineResponse response = new EventTimelineResponse();
        response.setId(timeline.getId());
        response.setEventId(timeline.getEvent().getId());
        response.setType(timeline.getType());
        response.setDescription(timeline.getDescription());
        response.setCreatedAt(timeline.getCreatedAt());
        return response;
    }
}
