package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByEventIdOrderByCreatedAtDesc(Long eventId);

    List<Announcement> findByCharityIdOrderByCreatedAtDesc(Long charityId);
}
