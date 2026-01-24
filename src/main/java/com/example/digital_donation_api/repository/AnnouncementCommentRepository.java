package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.AnnouncementComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementCommentRepository
        extends JpaRepository<AnnouncementComment, Long> {

    List<AnnouncementComment> findByAnnouncementIdOrderByCreatedAtAsc(Long announcementId);
}
