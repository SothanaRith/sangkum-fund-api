package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTimelineRepository extends JpaRepository<EventTimeline, Long> {

    List<EventTimeline> findByEventIdOrderByCreatedAtAsc(Long eventId);
}
