package com.example.digital_donation_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_images")
@Getter
@Setter
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    private String imageUrl;
    
    private Boolean isPrimary = false;
    
    private Integer displayOrder = 0;

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
