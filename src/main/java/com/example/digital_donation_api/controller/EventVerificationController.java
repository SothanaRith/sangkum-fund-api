package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/event-verification")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EventVerificationController {

    private final EventService eventService;

    @GetMapping("/pending")
    public ResponseEntity<List<Event>> getPendingEvents() {
        List<Event> pendingEvents = eventService.getPendingEvents();
        return ResponseEntity.ok(pendingEvents);
    }

    @PostMapping("/{eventId}/approve")
    public ResponseEntity<Map<String, Object>> approveEvent(
            @PathVariable Long eventId,
            Authentication authentication) {
        
        Long adminId = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal())
                .getUsername() != null ? 1L : 1L; // TODO: Get actual user ID from authentication
        
        Event event = eventService.approveEvent(eventId, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event approved successfully");
        response.put("event", event);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/reject")
    public ResponseEntity<Map<String, Object>> rejectEvent(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Rejection reason is required"
            ));
        }
        
        Long adminId = 1L; // TODO: Get actual user ID from authentication
        
        Event event = eventService.rejectEvent(eventId, adminId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event rejected");
        response.put("event", event);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/status")
    public ResponseEntity<Map<String, Object>> getEventStatus(@PathVariable Long eventId) {
        Event event = eventService.getById(eventId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", event.getId());
        response.put("title", event.getTitle());
        response.put("status", event.getStatus());
        response.put("rejectionReason", event.getRejectionReason());
        response.put("reviewedAt", event.getReviewedAt());
        
        return ResponseEntity.ok(response);
    }
}
