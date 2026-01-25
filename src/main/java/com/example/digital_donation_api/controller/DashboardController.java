package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public DashboardController(DonationRepository donationRepository, 
                              EventRepository eventRepository,
                              UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> analytics = new HashMap<>();

        // Get donations by month (last 6 months)
        List<Map<String, Object>> donationsByMonth = getDonationsByMonth(user.getId());
        analytics.put("donationsByMonth", donationsByMonth);

        // Get donations by status
        List<Map<String, Object>> donationsByStatus = getDonationsByStatus(user.getId());
        analytics.put("donationsByStatus", donationsByStatus);

        // Get top events by donation amount
        List<Map<String, Object>> topEvents = getTopEvents(user.getId());
        analytics.put("topEvents", topEvents);

        // Get donation trends (day by day for last 30 days)
        List<Map<String, Object>> donationTrends = getDonationTrends(user.getId());
        analytics.put("donationTrends", donationTrends);

        // Get my events performance
        List<Map<String, Object>> eventsPerformance = getEventsPerformance(user.getId());
        analytics.put("eventsPerformance", eventsPerformance);

        analytics.put("success", true);
        return ResponseEntity.ok(analytics);
    }

    private List<Map<String, Object>> getDonationsByMonth(Long userId) {
        List<Object[]> results = donationRepository.findDonationsByMonth(userId, 6);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]); // e.g., "2026-01"
            map.put("count", ((Number) row[1]).intValue());
            map.put("total", ((Number) row[2]).doubleValue());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDonationsByStatus(Long userId) {
        List<Object[]> results = donationRepository.countByStatusAndUserId(userId);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("status", row[0]);
            map.put("count", ((Number) row[1]).intValue());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getTopEvents(Long userId) {
        List<Object[]> results = donationRepository.findTopEventsByUser(userId);
        return results.stream().limit(5).map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("eventName", row[0]);
            map.put("total", ((Number) row[1]).doubleValue());
            map.put("count", ((Number) row[2]).intValue());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDonationTrends(Long userId) {
        List<Object[]> results = donationRepository.findDonationTrends(userId, 30);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("count", ((Number) row[1]).intValue());
            map.put("total", ((Number) row[2]).doubleValue());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getEventsPerformance(Long userId) {
        List<Object[]> results = eventRepository.findEventsPerformanceByUser(userId);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("eventName", row[0]);
            map.put("goalAmount", ((Number) row[1]).doubleValue());
            map.put("raised", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
            map.put("donationsCount", row[3] != null ? ((Number) row[3]).intValue() : 0);
            map.put("progress", row[2] != null && row[1] != null && ((Number) row[1]).doubleValue() > 0 
                ? Math.round((((Number) row[2]).doubleValue() / ((Number) row[1]).doubleValue()) * 100) 
                : 0);
            return map;
        }).collect(Collectors.toList());
    }
}
