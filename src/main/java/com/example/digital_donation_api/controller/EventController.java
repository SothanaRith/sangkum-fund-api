package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.AnnouncementMapper;
import com.example.digital_donation_api.dto.mapper.DonationMapper;
import com.example.digital_donation_api.dto.mapper.EventCommentMapper;
import com.example.digital_donation_api.dto.mapper.EventMapper;
import com.example.digital_donation_api.dto.mapper.EventTimelineMapper;
import com.example.digital_donation_api.dto.request.EventCommentCreateRequest;
import com.example.digital_donation_api.dto.request.EventCreateRequest;
import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.dto.response.DonationResponse;
import com.example.digital_donation_api.dto.response.EventCommentResponse;
import com.example.digital_donation_api.dto.response.EventParticipantResponse;
import com.example.digital_donation_api.dto.response.EventResponse;
import com.example.digital_donation_api.dto.response.EventTimelineResponse;
import com.example.digital_donation_api.entity.Announcement;
import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventComment;
import com.example.digital_donation_api.entity.EventTimeline;
import com.example.digital_donation_api.entity.EventVisibility;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.repository.DonationRepository;
import com.example.digital_donation_api.repository.EventCommentRepository;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventTimelineRepository;
import com.example.digital_donation_api.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMemberRepository eventMemberRepository;
    private final EventTimelineRepository eventTimelineRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventCommentRepository eventCommentRepository;
    private final DonationRepository donationRepository;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setGoalAmount(request.getTargetAmount());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setImageUrl(request.getImageUrl());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setCategory(request.getCategory());
        
        // Set visibility with default PUBLIC
        if (request.getVisibility() != null && request.getVisibility().equalsIgnoreCase("PRIVATE")) {
            event.setVisibility(EventVisibility.PRIVATE);
        } else {
            event.setVisibility(EventVisibility.PUBLIC);
        }
        
        Event createdEvent = eventService.create(event, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventMapper.toResponse(createdEvent, eventMemberRepository));
    }
    
    @PostMapping("/{eventId}/submit")
    public ResponseEntity<?> submitForApproval(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Event event = eventService.submitForApproval(eventId, user.getId());
        
        return ResponseEntity.ok(java.util.Map.of(
            "success", true,
            "message", "Event submitted for admin approval",
            "event", EventMapper.toResponse(event, eventMemberRepository)
        ));
    }
    
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        
        Event eventData = new Event();
        eventData.setTitle(request.getTitle());
        eventData.setDescription(request.getDescription());
        eventData.setGoalAmount(request.getTargetAmount());
        eventData.setStartDate(request.getStartDate());
        eventData.setEndDate(request.getEndDate());
        eventData.setImageUrl(request.getImageUrl());
        eventData.setLocation(request.getLocation());
        eventData.setLatitude(request.getLatitude());
        eventData.setLongitude(request.getLongitude());
        eventData.setCategory(request.getCategory());
        eventData.setStartDate(request.getStartDate());
        eventData.setEndDate(request.getEndDate());
        eventData.setImageUrl(request.getImageUrl());
        
        // Set visibility
        if (request.getVisibility() != null && request.getVisibility().equalsIgnoreCase("PRIVATE")) {
            eventData.setVisibility(EventVisibility.PRIVATE);
        } else {
            eventData.setVisibility(EventVisibility.PUBLIC);
        }
        
        Event updatedEvent = eventService.update(eventId, eventData, user.getId());
        return ResponseEntity.ok(EventMapper.toResponse(updatedEvent, eventMemberRepository));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        Event event = eventService.getById(id);
        return ResponseEntity.ok(EventMapper.toResponse(event, eventMemberRepository));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPublicActiveEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Event> eventPage = eventService.getPublicActiveEvents(pageable);
        List<EventResponse> responses = eventPage.getContent().stream()
                .map(e -> EventMapper.toResponse(e, eventMemberRepository))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", responses);
        response.put("currentPage", eventPage.getNumber());
        response.put("totalPages", eventPage.getTotalPages());
        response.put("totalElements", eventPage.getTotalElements());
        response.put("size", eventPage.getSize());
        response.put("hasNext", eventPage.hasNext());
        response.put("hasPrevious", eventPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/donations")
    public ResponseEntity<List<DonationResponse>> getRecentDonationsByEvent(@PathVariable Long eventId) {
        List<Donation> donations = donationRepository.findTop5ByEventIdOrderByCreatedAtDesc(eventId);
        List<DonationResponse> responses = donations.stream()
                .map(DonationMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/my-events")
    public ResponseEntity<Map<String, Object>> getMyEvents(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Event> eventPage = eventService.getMyEvents(user.getId(), pageable);
        List<EventResponse> responses = eventPage.getContent().stream()
                .map(e -> EventMapper.toResponse(e, eventMemberRepository))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", responses);
        response.put("currentPage", eventPage.getNumber());
        response.put("totalPages", eventPage.getTotalPages());
        response.put("totalElements", eventPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/join")
    public ResponseEntity<Map<String, Object>> joinEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        eventService.joinEvent(eventId, user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully joined the event");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<List<EventParticipantResponse>> getEventParticipants(@PathVariable Long eventId) {
        List<EventParticipantResponse> participants = eventService.getEventParticipants(eventId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/{eventId}/timeline")
    public ResponseEntity<List<EventTimelineResponse>> getEventTimeline(@PathVariable Long eventId) {
        List<EventTimeline> timelines = eventTimelineRepository.findByEventIdOrderByCreatedAtAsc(eventId);
        List<EventTimelineResponse> responses = timelines.stream()
                .map(EventTimelineMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{eventId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getEventAnnouncements(@PathVariable Long eventId) {
        List<Announcement> announcements = announcementRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        List<AnnouncementResponse> responses = announcements.stream()
                .map(AnnouncementMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{eventId}/comments")
    public ResponseEntity<List<EventCommentResponse>> getEventComments(@PathVariable Long eventId) {
        List<EventComment> comments = eventCommentRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        List<EventCommentResponse> responses = comments.stream()
                .map(EventCommentMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{eventId}/comments")
    public ResponseEntity<EventCommentResponse> createComment(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCommentCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Event event = eventService.getById(eventId);
        
        EventComment comment = new EventComment();
        comment.setEvent(event);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        EventComment savedComment = eventCommentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventCommentMapper.toResponse(savedComment));
    }
}
