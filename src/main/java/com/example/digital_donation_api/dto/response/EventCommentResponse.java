package com.example.digital_donation_api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCommentResponse {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private LocalDateTime createdAt;
}
