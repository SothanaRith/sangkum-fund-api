package com.example.digital_donation_api.entity;

public enum EventStatus {
    DRAFT,          // Event is being created
    PENDING,        // Submitted for admin approval
    APPROVED,       // Approved by admin and can be active
    REJECTED,       // Rejected by admin
    ACTIVE,         // Currently running
    PAUSED,         // Temporarily paused
    COMPLETED,      // Event ended
    CANCELLED,      // Event cancelled
    BLOCKED         // Blocked by admin for policy violations
}
