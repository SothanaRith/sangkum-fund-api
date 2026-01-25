package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "charities")
@Getter
@Setter
public class Charity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logo;
    
    private String registrationNumber;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    private String contactEmail;
    
    private String contactPhone;
    
    private String website;
    
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private CharityStatus status = CharityStatus.PENDING;

    private LocalDateTime verifiedAt;
}
