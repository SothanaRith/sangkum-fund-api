package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Announcement;

public interface AnnouncementService {

    Announcement create(Announcement announcement, Long authorId);
}
