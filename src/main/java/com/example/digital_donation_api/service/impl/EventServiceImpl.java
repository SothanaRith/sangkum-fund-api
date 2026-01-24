package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventMember;
import com.example.digital_donation_api.entity.EventStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMemberRepository eventMemberRepository;

    @Override
    public Event create(Event event, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        event.setOwner(owner);
        event.setStatus(EventStatus.ACTIVE);

        return eventRepository.save(event);
    }

    @Override
    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Override
    public List<Event> getPublicActiveEvents() {
        return eventRepository.findPublicActiveEvents();
    }
    
    @Override
    public List<Event> getMyEvents(Long userId) {
        return eventRepository.findByOwnerId(userId);
    }

    @Override
    public void joinEvent(Long eventId, Long userId) {
        if (eventMemberRepository.existsByEventIdAndUserId(eventId, userId)) {
            return;
        }

        Event event = getById(eventId);
        User user = userRepository.findById(userId).orElseThrow();

        EventMember member = new EventMember();
        member.setEvent(event);
        member.setUser(user);

        eventMemberRepository.save(member);
    }
}
