package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.response.AdminDashboardStatsResponse;
import com.example.digital_donation_api.dto.response.ActivityLogResponse;
import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final EventRepository eventRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AnnouncementRepository announcementRepository;
    private final CharityRepository charityRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        try {
            // Count totals
            long totalEvents = eventRepository.count();
            long pendingEvents = eventRepository.countByStatus(EventStatus.PENDING);
            long totalDonations = donationRepository.count();
            long totalUsers = userRepository.count();
            long unreadNotifications = notificationRepository.count(); // Assume we can filter by read status
            long newsItems = announcementRepository.count();
            
            // Sum donation amounts
            BigDecimal totalAmount = donationRepository.findAll().stream()
                    .map(Donation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Count active users (let's assume users with events or donations in the last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long activeUsers = eventRepository.findAll().stream()
                    .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(thirtyDaysAgo))
                    .filter(e -> e.getOwner() != null)
                    .map(e -> e.getOwner().getId())
                    .distinct()
                    .count();
            
            // If that's not enough, also count users who have made donations
            long donatingUsers = donationRepository.findAll().stream()
                    .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(thirtyDaysAgo))
                    .filter(d -> d.getUser() != null)
                    .map(d -> d.getUser().getId())
                    .distinct()
                    .count();
            
            activeUsers = Math.max(activeUsers, donatingUsers);
            
            // Count pending approvals for charities and events
            long charityApprovals = charityRepository.findAll().stream()
                    .filter(c -> c.getStatus() == CharityStatus.PENDING)
                    .count();
            
            long eventApprovals = pendingEvents;
            
            // Build stats totals
            AdminDashboardStatsResponse.StatsTotals totals = AdminDashboardStatsResponse.StatsTotals.builder()
                    .totalEvents(totalEvents)
                    .pendingEvents(pendingEvents)
                    .totalDonations(totalDonations)
                    .totalAmount(totalAmount)
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .unreadNotifications(unreadNotifications)
                    .newsItems(newsItems)
                    .eventApprovals(eventApprovals)
                    .charityApprovals(charityApprovals)
                    .build();
            
            // Build system health metrics
            AdminDashboardStatsResponse.SystemHealth health = AdminDashboardStatsResponse.SystemHealth.builder()
                    .apiResponseTime(95.0)  // Placeholder - in production, measure actual response times
                    .dbLoad(calculateDbLoad())
                    .storageUsage(calculateStorageUsage())
                    .activeSessions(countActiveSessions())
                    .build();
            
            // Get recent activity
            List<ActivityLogResponse> recentActivity = getRecentActivity(10);
            
            // Build response
            AdminDashboardStatsResponse response = AdminDashboardStatsResponse.builder()
                    .totals(totals)
                    .health(health)
                    .recentActivity(recentActivity)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private Double calculateDbLoad() {
        // Simplified calculation - in production, query database metrics
        // For now, return a reasonable estimate based on record count
        long totalRecords = eventRepository.count() + donationRepository.count() + userRepository.count();
        return Math.min(100.0, (totalRecords / 10000.0) * 100);
    }

    private Double calculateStorageUsage() {
        // Placeholder - in production, check actual disk usage
        return 62.0;
    }

    private Long countActiveSessions() {
        // Count users with recent activity (events or donations in last 5 minutes)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long recentEventCreators = eventRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(fiveMinutesAgo))
                .filter(e -> e.getOwner() != null)
                .map(e -> e.getOwner().getId())
                .distinct()
                .count();
        
        long recentDonors = donationRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(fiveMinutesAgo))
                .map(d -> d.getUser().getId())
                .distinct()
                .count();
        
        return Math.max(recentEventCreators, recentDonors);
    }

    private List<ActivityLogResponse> getRecentActivity(int limit) {
        List<ActivityLog> logs = activityLogRepository.findAll();
        
        // Sort by created date descending and take limit
        return logs.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(log -> ActivityLogResponse.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .details(log.getTargetType() + " (" + log.getTargetId() + ")")
                        .type(log.getTargetType())
                        .createdAt(log.getCreatedAt())
                        .userName(log.getUser() != null ? log.getUser().getUsername() : "System")
                        .userEmail(log.getUser() != null ? log.getUser().getEmail() : "system@admin.com")
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = eventRepository.count();
        long pending = eventRepository.countByStatus(EventStatus.PENDING);
        long approved = eventRepository.countByStatus(EventStatus.APPROVED);
        long active = eventRepository.countByStatus(EventStatus.ACTIVE);
        long completed = eventRepository.countByStatus(EventStatus.COMPLETED);
        long rejected = eventRepository.countByStatus(EventStatus.REJECTED);
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("active", active);
        stats.put("completed", completed);
        stats.put("rejected", rejected);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/donations")
    public ResponseEntity<Map<String, Object>> getDonationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Donation> donations = donationRepository.findAll();
        
        stats.put("total", donations.size());
        stats.put("totalAmount", donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("avgAmount", donations.isEmpty() ? BigDecimal.ZERO :
                donations.stream()
                        .map(Donation::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(donations.size()), BigDecimal.ROUND_HALF_UP));
        
        stats.put("statusBreakdown", donations.stream()
                .collect(Collectors.groupingByConcurrent(
                        Donation::getStatus,
                        Collectors.counting()
                )));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> users = userRepository.findAll();
        long total = users.size();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // Count active users (those with recent events or donations)
        long active = Math.max(
            eventRepository.findAll().stream()
                    .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(thirtyDaysAgo))
                    .filter(e -> e.getOwner() != null)
                    .map(e -> e.getOwner().getId())
                    .distinct()
                    .count(),
            donationRepository.findAll().stream()
                    .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(thirtyDaysAgo))
                    .map(d -> d.getUser().getId())
                    .distinct()
                    .count()
        );
        
        stats.put("total", total);
        stats.put("active", active);
        stats.put("inactive", total - active);
        
        return ResponseEntity.ok(stats);
    }
}
