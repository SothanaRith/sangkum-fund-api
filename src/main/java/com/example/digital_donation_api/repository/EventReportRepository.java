package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventReportRepository extends JpaRepository<EventReport, Long> {

    Optional<EventReport> findByEventId(Long eventId);
}
