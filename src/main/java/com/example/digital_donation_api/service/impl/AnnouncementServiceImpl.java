package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Announcement;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @Override
    public Announcement create(Announcement announcement, Long authorId) {
        User author = userRepository.findById(authorId).orElseThrow();
        announcement.setAuthor(author);
        return announcementRepository.save(announcement);
    }
}
