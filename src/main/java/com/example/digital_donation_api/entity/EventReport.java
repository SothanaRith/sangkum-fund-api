package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_reports")
@Getter
@Setter
public class EventReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "event_id", unique = true)
    private Event event;

    private BigDecimal totalDonations = BigDecimal.ZERO;
    private Integer totalDonors = 0;
    private Integer totalMembers = 0;

    private LocalDateTime lastUpdatedAt;
}
