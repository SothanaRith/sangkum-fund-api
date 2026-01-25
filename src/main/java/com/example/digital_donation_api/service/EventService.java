package com.example.digital_donation_api.service;

import com.example.digital_donation_api.dto.response.EventParticipantResponse;
import com.example.digital_donation_api.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventService {

    Event create(Event event, Long ownerId);
    
    Event update(Long eventId, Event eventData, Long ownerId);

    Event getById(Long id);

    Page<Event> getPublicActiveEvents(Pageable pageable);
    
    Page<Event> getMyEvents(Long userId, Pageable pageable);

    void joinEvent(Long eventId, Long userId);
    
    List<EventParticipantResponse> getEventParticipants(Long eventId);
    
    // Admin verification methods
    List<Event> getPendingEvents();
    
    Event approveEvent(Long eventId, Long adminId);
    
    Event rejectEvent(Long eventId, Long adminId, String reason);
    
    Event submitForApproval(Long eventId, Long ownerId);
}
