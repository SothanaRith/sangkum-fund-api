package com.example.digital_donation_api.entity;

public enum CharityStatus {
    PENDING,      // Awaiting verification
    VERIFIED,     // Verified charity
    REJECTED,     // Verification rejected
    SUSPENDED,    // Temporarily suspended
    BLOCKED       // Blocked by admin for policy violations
}
