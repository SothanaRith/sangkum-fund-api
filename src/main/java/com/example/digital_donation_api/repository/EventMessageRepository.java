package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventMessageRepository extends JpaRepository<EventMessage, Long> {

    Page<EventMessage> findByEventIdOrderByCreatedAtAsc(Long eventId, Pageable pageable);

    List<EventMessage> findByEventIdOrderByCreatedAtAsc(Long eventId, org.springframework.data.domain.Sort sort);

    long countByEventId(Long eventId);
}
