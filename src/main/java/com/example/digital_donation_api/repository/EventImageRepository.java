package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    
    List<EventImage> findByEventIdOrderByDisplayOrderAsc(Long eventId);
    
    void deleteByEventIdAndId(Long eventId, Long imageId);
}
