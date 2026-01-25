package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImageUrl;
    private List<String> tags;
    private String status;
    private Boolean featured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
}
