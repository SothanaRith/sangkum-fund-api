package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Event;

import java.util.List;

public interface EventService {

    Event create(Event event, Long ownerId);

    Event getById(Long id);

    List<Event> getPublicActiveEvents();
    
    List<Event> getMyEvents(Long userId);

    void joinEvent(Long eventId, Long userId);
}
