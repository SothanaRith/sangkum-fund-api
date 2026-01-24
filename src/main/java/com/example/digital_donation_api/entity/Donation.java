package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter
@Setter
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable for anonymous

    private BigDecimal amount;
    private String currency = "USD";

    private Boolean isAnonymous = false;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String transactionRef;
    private String qrCodeData; // For KHQR or OFFLINE_QR payment

    @Enumerated(EnumType.STRING)
    private DonationStatus status = DonationStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
}
