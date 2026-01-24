package com.example.digital_donation_api.entity;

public enum PaymentProofStatus {
    PENDING,    // Awaiting admin verification
    APPROVED,   // Payment verified and approved
    REJECTED    // Payment proof rejected
}
