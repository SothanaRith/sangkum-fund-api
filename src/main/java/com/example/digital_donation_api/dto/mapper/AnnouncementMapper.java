package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.entity.Announcement;

public class AnnouncementMapper {
    public static AnnouncementResponse toResponse(Announcement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getAuthor().getId(),
                a.getCreatedAt()
        );
    }
}
