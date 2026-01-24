package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {

    List<EventComment> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
