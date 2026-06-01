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
import com.example.digital_donation_api.entity.EventMessage;
import com.example.digital_donation_api.entity.EventTimeline;
import com.example.digital_donation_api.entity.EventVisibility;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.repository.DonationRepository;
import com.example.digital_donation_api.repository.EventCommentRepository;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventMessageRepository;
import com.example.digital_donation_api.repository.AnnouncementReactionRepository;
import com.example.digital_donation_api.repository.AnnouncementCommentRepository;
import com.example.digital_donation_api.repository.AnnouncementCommentReactionRepository;
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
    private final EventMessageRepository eventMessageRepository;
    private final AnnouncementReactionRepository announcementReactionRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;
    private final AnnouncementCommentReactionRepository announcementCommentReactionRepository;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
        event.setKhqrImage(request.getKhqrImage());
        event.setBakongAccountId(request.getBakongAccountId());
        
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
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
        eventData.setKhqrImage(request.getKhqrImage());
        eventData.setBakongAccountId(request.getBakongAccountId());
        
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
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
                .map(a -> AnnouncementMapper.toResponse(a, announcementCommentRepository, announcementReactionRepository, announcementCommentReactionRepository))
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
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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

    // ── View tracking ─────────────────────────────────────────────────────────

    @PostMapping("/{eventId}/view")
    public ResponseEntity<Map<String, Object>> recordView(@PathVariable Long eventId) {
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{eventId}/view/anonymous")
    public ResponseEntity<Map<String, Object>> recordAnonymousView(@PathVariable Long eventId) {
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Event messages (chat) ─────────────────────────────────────────────────

    @PostMapping("/{eventId}/messages")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String text = body.get("message");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Message cannot be empty"));
        }
        User user = (User) authentication.getPrincipal();
        Event event = eventService.getById(eventId);

        EventMessage msg = new EventMessage();
        msg.setEvent(event);
        msg.setUser(user);
        msg.setMessage(text.trim());
        EventMessage saved = eventMessageRepository.save(msg);

        boolean isOwner = event.getOwner() != null && event.getOwner().getId().equals(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toMessageMap(saved, isOwner));
    }

    @GetMapping("/{eventId}/messages")
    public ResponseEntity<Map<String, Object>> getMessages(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<EventMessage> messagePage = eventMessageRepository.findByEventIdOrderByCreatedAtAsc(eventId, pageable);

        Event event = eventService.getById(eventId);
        List<Map<String, Object>> content = messagePage.getContent().stream()
                .map(m -> {
                    boolean isOwner = event.getOwner() != null && event.getOwner().getId().equals(m.getUser().getId());
                    return toMessageMap(m, isOwner);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "content", content,
                "totalElements", messagePage.getTotalElements(),
                "totalPages", messagePage.getTotalPages(),
                "currentPage", messagePage.getNumber()
        ));
    }

    @GetMapping("/{eventId}/messages/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentMessages(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").ascending());
        Event event = eventService.getById(eventId);
        List<Map<String, Object>> messages = eventMessageRepository
                .findByEventIdOrderByCreatedAtAsc(eventId, pageable)
                .getContent().stream()
                .map(m -> {
                    boolean isOwner = event.getOwner() != null && event.getOwner().getId().equals(m.getUser().getId());
                    return toMessageMap(m, isOwner);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{eventId}/messages/count")
    public ResponseEntity<Map<String, Object>> getMessageCount(@PathVariable Long eventId) {
        long count = eventMessageRepository.countByEventId(eventId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @DeleteMapping("/{eventId}/messages/{messageId}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long eventId,
            @PathVariable Long messageId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        EventMessage msg = eventMessageRepository.findById(messageId)
                .orElseThrow(() -> new com.example.digital_donation_api.exception.ResourceNotFoundException("Message not found"));

        Event event = eventService.getById(eventId);
        boolean isOwner = event.getOwner() != null && event.getOwner().getId().equals(user.getId());
        boolean isAuthor = msg.getUser().getId().equals(user.getId());

        if (!isAuthor && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Not allowed"));
        }
        eventMessageRepository.delete(msg);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> toMessageMap(EventMessage msg, boolean isOwner) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", msg.getId());
        m.put("userId", msg.getUser().getId());
        m.put("userName", msg.getUser().getName());
        m.put("userAvatar", msg.getUser().getAvatar());
        m.put("message", msg.getMessage());
        m.put("isOwner", isOwner);
        m.put("createdAt", msg.getCreatedAt());
        return m;
    }
}
