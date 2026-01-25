package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String coverImageUrl;

    /**
     * Comma-separated lowercase tags (e.g., "health,education").
     */
    @Column(length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PostStatus status = PostStatus.DRAFT;

    private Boolean featured = false;

    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
