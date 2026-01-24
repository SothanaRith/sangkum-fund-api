package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.AnnouncementMapper;
import com.example.digital_donation_api.dto.mapper.EventCommentMapper;
import com.example.digital_donation_api.dto.mapper.EventMapper;
import com.example.digital_donation_api.dto.mapper.EventTimelineMapper;
import com.example.digital_donation_api.dto.request.EventCommentCreateRequest;
import com.example.digital_donation_api.dto.request.EventCreateRequest;
import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.dto.response.EventCommentResponse;
import com.example.digital_donation_api.dto.response.EventResponse;
import com.example.digital_donation_api.dto.response.EventTimelineResponse;
import com.example.digital_donation_api.entity.Announcement;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventComment;
import com.example.digital_donation_api.entity.EventTimeline;
import com.example.digital_donation_api.entity.EventVisibility;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.repository.EventCommentRepository;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventTimelineRepository;
import com.example.digital_donation_api.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        
        // Set visibility with default PUBLIC
        if (request.getVisibility() != null && request.getVisibility().equalsIgnoreCase("PRIVATE")) {
            event.setVisibility(EventVisibility.PRIVATE);
        } else {
            event.setVisibility(EventVisibility.PUBLIC);
        }
        
        Event createdEvent = eventService.create(event, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventMapper.toResponse(createdEvent, eventMemberRepository));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        Event event = eventService.getById(id);
        return ResponseEntity.ok(EventMapper.toResponse(event, eventMemberRepository));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getPublicActiveEvents() {
        List<Event> events = eventService.getPublicActiveEvents();
        List<EventResponse> responses = events.stream()
                .map(e -> EventMapper.toResponse(e, eventMemberRepository))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/my-events")
    public ResponseEntity<List<EventResponse>> getMyEvents(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Event> events = eventService.getMyEvents(user.getId());
        List<EventResponse> responses = events.stream()
                .map(e -> EventMapper.toResponse(e, eventMemberRepository))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{eventId}/join")
    public ResponseEntity<String> joinEvent(@PathVariable Long eventId) {
        // TODO: Get current user ID from security context
        // For now, we'll need authentication implementation
        return ResponseEntity.ok("Join endpoint - authentication needed");
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
