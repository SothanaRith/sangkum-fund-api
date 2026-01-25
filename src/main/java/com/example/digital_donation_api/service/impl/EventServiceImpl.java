package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.dto.response.EventParticipantResponse;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventMember;
import com.example.digital_donation_api.entity.EventStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.DonationRepository;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMemberRepository eventMemberRepository;
    private final DonationRepository donationRepository;

    @Override
    public Event create(Event event, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        event.setOwner(owner);
        // Set to PENDING for admin approval
        event.setStatus(EventStatus.PENDING);

        return eventRepository.save(event);
    }

    @Override
    public Event update(Long eventId, Event eventData, Long ownerId) {
        Event event = getById(eventId);
        
        // Verify ownership
        if (!event.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only event owner can update the event");
        }
        
        // Update allowed fields
        if (eventData.getTitle() != null) {
            event.setTitle(eventData.getTitle());
        }
        if (eventData.getDescription() != null) {
            event.setDescription(eventData.getDescription());
        }
        if (eventData.getGoalAmount() != null) {
            event.setGoalAmount(eventData.getGoalAmount());
        }
        if (eventData.getStartDate() != null) {
            event.setStartDate(eventData.getStartDate());
        }
        if (eventData.getEndDate() != null) {
            event.setEndDate(eventData.getEndDate());
        }
        if (eventData.getImageUrl() != null) {
            event.setImageUrl(eventData.getImageUrl());
        }
        if (eventData.getVisibility() != null) {
            event.setVisibility(eventData.getVisibility());
        }
        if (eventData.getLocation() != null) {
            event.setLocation(eventData.getLocation());
        }
        if (eventData.getLatitude() != null) {
            event.setLatitude(eventData.getLatitude());
        }
        if (eventData.getLongitude() != null) {
            event.setLongitude(eventData.getLongitude());
        }
        if (eventData.getCategory() != null) {
            event.setCategory(eventData.getCategory());
        }
        
        // If event was rejected and now being updated, set back to PENDING for re-approval
        if (event.getStatus() == EventStatus.REJECTED) {
            event.setStatus(EventStatus.PENDING);
            event.setRejectionReason(null);
        }
        
        return eventRepository.save(event);
    }

    @Override
    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Override
    public Page<Event> getPublicActiveEvents(Pageable pageable) {
        return eventRepository.findPublicActiveEvents(pageable);
    }
    
    @Override
    public Page<Event> getMyEvents(Long userId, Pageable pageable) {
        return eventRepository.findByOwnerId(userId, pageable);
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

    @Override
    public List<EventParticipantResponse> getEventParticipants(Long eventId) {
        // Verify event exists
        Event event = getById(eventId);
        
        // Get all event members
        List<EventMember> members = eventMemberRepository.findByEventId(eventId);
        
        return members.stream().map(member -> {
            User user = member.getUser();
            
            // Get donation stats for this participant
            BigDecimal totalDonated = donationRepository.sumSuccessfulDonationsByEventAndUser(eventId, user.getId());
            Long donationCount = donationRepository.countSuccessfulDonationsByEventAndUser(eventId, user.getId());
            
            return new EventParticipantResponse(
                user.getId(),
                user.getName(),
                user.getAvatar(),
                member.getJoinedAt(),
                totalDonated != null ? totalDonated : BigDecimal.ZERO,
                donationCount != null ? donationCount.intValue() : 0
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }

    @Override
    public Event approveEvent(Long eventId, Long adminId) {
        Event event = getById(eventId);
        
        if (event.getStatus() != EventStatus.PENDING) {
            throw new IllegalStateException("Only pending events can be approved");
        }
        
        event.setStatus(EventStatus.APPROVED);
        event.setReviewedBy(adminId);
        event.setReviewedAt(java.time.LocalDateTime.now());
        event.setRejectionReason(null);
        
        return eventRepository.save(event);
    }

    @Override
    public Event rejectEvent(Long eventId, Long adminId, String reason) {
        Event event = getById(eventId);
        
        if (event.getStatus() != EventStatus.PENDING) {
            throw new IllegalStateException("Only pending events can be rejected");
        }
        
        event.setStatus(EventStatus.REJECTED);
        event.setReviewedBy(adminId);
        event.setReviewedAt(java.time.LocalDateTime.now());
        event.setRejectionReason(reason);
        
        return eventRepository.save(event);
    }

    @Override
    public Event submitForApproval(Long eventId, Long ownerId) {
        Event event = getById(eventId);
        
        if (!event.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only event owner can submit for approval");
        }
        
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
            throw new IllegalStateException("Only draft or rejected events can be submitted for approval");
        }
        
        event.setStatus(EventStatus.PENDING);
        event.setRejectionReason(null);
        
        return eventRepository.save(event);
    }
}
