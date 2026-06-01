package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.AnnouncementCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementCommentReactionRepository extends JpaRepository<AnnouncementCommentReaction, Long> {

    List<AnnouncementCommentReaction> findByCommentId(Long commentId);

    Optional<AnnouncementCommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);
}
