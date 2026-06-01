package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorDto author;
    private EventDto event;
    private CharityDto charity;
    private List<CommentDto> comments;
    private List<ReactionDto> reactions;
    private LocalDateTime createdAt;

    @AllArgsConstructor
    @Getter
    public static class AuthorDto {
        private Long id;
        private String name;
    }

    @AllArgsConstructor
    @Getter
    public static class EventDto {
        private Long id;
        private String title;
    }

    @AllArgsConstructor
    @Getter
    public static class CharityDto {
        private Long id;
        private String name;
    }

    @AllArgsConstructor
    @Getter
    public static class CommentDto {
        private Long id;
        private String content;
        private Long userId;
        private String authorName;
        private String userAvatar;
        private Long parentId;
        private LocalDateTime createdAt;
        private List<ReactionDto> reactions;
        private List<CommentDto> replies;
    }

    @AllArgsConstructor
    @Getter
    public static class ReactionDto {
        private Long id;
        private String type;
        private Long userId;
    }
}
