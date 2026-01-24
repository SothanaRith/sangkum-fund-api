package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.AnnouncementReaction;
import com.example.digital_donation_api.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementReactionRepository
        extends JpaRepository<AnnouncementReaction, Long> {

    long countByAnnouncementIdAndType(Long announcementId, ReactionType type);

    boolean existsByAnnouncementIdAndUserIdAndType(
            Long announcementId,
            Long userId,
            ReactionType type
    );
}
