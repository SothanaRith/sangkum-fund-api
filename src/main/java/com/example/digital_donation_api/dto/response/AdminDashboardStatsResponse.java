package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    
    private StatsTotals totals;
    private SystemHealth health;
    private List<ActivityLogResponse> recentActivity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsTotals {
        private Long totalEvents;
        private Long pendingEvents;
        private Long totalDonations;
        private BigDecimal totalAmount;
        private Long totalUsers;
        private Long activeUsers;
        private Long unreadNotifications;
        private Long newsItems;
        private Long eventApprovals;
        private Long charityApprovals;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private Double apiResponseTime;  // percentage (0-100)
        private Double dbLoad;           // percentage (0-100)
        private Double storageUsage;     // percentage (0-100)
        private Long activeSessions;
    }
}
