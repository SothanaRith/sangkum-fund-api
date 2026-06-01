package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "charity_id")
    private Charity charity;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String imageUrl;

    private BigDecimal goalAmount;
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EventVisibility visibility = EventVisibility.PUBLIC;

    private String joinCode;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "VARCHAR(20)")
    private EventStatus status = EventStatus.DRAFT;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private java.time.LocalDateTime reviewedAt;
    
    private String location;
    
    @Column(name = "khqr_image")
    private String khqrImage;
    
    @Column(name = "bakong_account_id")
    private String bakongAccountId;
    
    private Double latitude;
    
    private Double longitude;
    
    private String category;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images = new ArrayList<>();
}

