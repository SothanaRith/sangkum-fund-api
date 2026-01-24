package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_proofs")
@Getter
@Setter
public class PaymentProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(nullable = false)
    private String imageUrl; // URL or path to uploaded transaction image

    @Column(length = 500)
    private String notes; // Donor's notes about the payment

    private String transactionId; // Bank transaction ID if provided
    private String bankName;
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private PaymentProofStatus status = PaymentProofStatus.PENDING;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Admin verification fields
    @ManyToOne
    @JoinColumn(name = "verified_by")
    private User verifiedBy; // Admin who verified

    private LocalDateTime verifiedAt;
    
    @Column(length = 500)
    private String adminNotes; // Admin's verification notes
}
