package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<EventMember> findByEventId(Long eventId);

    long countByEventId(Long eventId);
}
