package com.example.digital_donation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "announcement_comment_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}) // User can only have 1 reaction per comment regardless of type
)
@Getter
@Setter
public class AnnouncementCommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private AnnouncementComment comment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReactionType type;
}
