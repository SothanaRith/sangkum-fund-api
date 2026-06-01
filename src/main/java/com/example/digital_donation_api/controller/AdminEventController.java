package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.EventMapper;
import com.example.digital_donation_api.dto.response.EventResponse;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.DonationRepository;
import com.example.digital_donation_api.repository.EventMemberRepository;
import com.example.digital_donation_api.repository.EventRepository;
import com.example.digital_donation_api.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final DonationRepository donationRepository;

    /**
     * Get all events with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Event> eventPage;
        
        if (status != null && !status.isEmpty()) {
            EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
            eventPage = eventRepository.findByStatus(eventStatus, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }
        
        // Filter by search term if provided
        List<EventResponse> responses = eventPage.getContent().stream()
                .filter(e -> {
                    if (search == null || search.isEmpty()) return true;
                    String searchLower = search.toLowerCase();
                    return e.getTitle().toLowerCase().contains(searchLower) ||
                           (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchLower)) ||
                           (e.getOwner() != null && e.getOwner().getName().toLowerCase().contains(searchLower));
                })
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

    /**
     * Get a specific event with full details
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getEventDetails(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        EventResponse eventResponse = EventMapper.toResponse(event, eventMemberRepository);
        
        // Calculate funding progress
        BigDecimal raisedAmount = donationRepository.sumSuccessfulDonations(eventId);
        if (raisedAmount == null) {
            raisedAmount = BigDecimal.ZERO;
        }
        
        long donorCount = donationRepository.countDonors(eventId);
        final BigDecimal finalRaisedAmount = raisedAmount;
        final long finalDonorCount = donorCount;
        
        Map<String, Object> details = new HashMap<>();
        details.put("event", eventResponse);
        details.put("fundingProgress", new HashMap<String, Object>() {{
            put("raised", finalRaisedAmount);
            put("goal", event.getGoalAmount());
            put("percentage", event.getGoalAmount().compareTo(BigDecimal.ZERO) > 0 ? 
                finalRaisedAmount.multiply(BigDecimal.valueOf(100))
                    .divide(event.getGoalAmount(), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO);
            put("donorCount", finalDonorCount);
        }});
        details.put("ownerName", event.getOwner() != null ? event.getOwner().getName() : "Unknown");
        details.put("ownerEmail", event.getOwner() != null ? event.getOwner().getEmail() : "Unknown");
        details.put("status", event.getStatus());
        
        return ResponseEntity.ok(details);
    }

    /**
     * Get pending events only
     */
    @GetMapping("/pending")
    public ResponseEntity<List<EventResponse>> getPendingEvents() {
        List<Event> pendingEvents = eventService.getPendingEvents();
        List<EventResponse> responses = pendingEvents.stream()
                .map(e -> EventMapper.toResponse(e, eventMemberRepository))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Approve a pending event
     */
    @PostMapping("/{eventId}/approve")
    public ResponseEntity<Map<String, Object>> approveEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        try {
            User admin = (User) authentication.getPrincipal();
            
            Event approvedEvent = eventService.approveEvent(eventId, admin.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event approved successfully");
            response.put("event", EventMapper.toResponse(approvedEvent, eventMemberRepository));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Reject a pending event
     */
    @PostMapping("/{eventId}/reject")
    public ResponseEntity<Map<String, Object>> rejectEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "No reason provided") String reason,
            Authentication authentication
    ) {
        try {
            User admin = (User) authentication.getPrincipal();
            
            Event rejectedEvent = eventService.rejectEvent(eventId, admin.getId(), reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event rejected");
            response.put("event", EventMapper.toResponse(rejectedEvent, eventMemberRepository));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete an event
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> deleteEvent(@PathVariable Long eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            
            eventRepository.delete(event);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get event by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EventResponse>> getEventsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
        PageRequest pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findByStatus(eventStatus, pageable);
        
        Page<EventResponse> responses = events.map(e -> EventMapper.toResponse(e, eventMemberRepository));
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Search events by title
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.searchEvents(query, pageable);
        
        Page<EventResponse> responses = events.map(e -> EventMapper.toResponse(e, eventMemberRepository));
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get funding progress for an event
     */
    @GetMapping("/{eventId}/funding")
    public ResponseEntity<Map<String, Object>> getFundingProgress(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        BigDecimal raisedAmount = donationRepository.sumSuccessfulDonations(eventId);
        if (raisedAmount == null) {
            raisedAmount = BigDecimal.ZERO;
        }
        
        long donorCount = donationRepository.countDonors(eventId);
        
        BigDecimal percentage = event.getGoalAmount().compareTo(BigDecimal.ZERO) > 0 ? 
            raisedAmount.multiply(BigDecimal.valueOf(100))
                .divide(event.getGoalAmount(), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
        
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("eventTitle", event.getTitle());
        response.put("goalAmount", event.getGoalAmount());
        response.put("raisedAmount", raisedAmount);
        response.put("percentage", percentage);
        response.put("donorCount", donorCount);
        response.put("remainingAmount", event.getGoalAmount().subtract(raisedAmount));
        response.put("status", event.getStatus());
        
        return ResponseEntity.ok(response);
    }
}
