package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.EventCommentResponse;
import com.example.digital_donation_api.entity.EventComment;

public class EventCommentMapper {

    public static EventCommentResponse toResponse(EventComment comment) {
        String userAvatar = comment.getUser().getAvatar();
        // Convert relative avatar URLs to absolute
        if (userAvatar != null && userAvatar.startsWith("/uploads/")) {
            userAvatar = "http://localhost:8080" + userAvatar;
        }
        
        EventCommentResponse response = new EventCommentResponse();
        response.setId(comment.getId());
        response.setEventId(comment.getEvent().getId());
        response.setUserId(comment.getUser().getId());
        response.setUserName(comment.getUser().getName());
        response.setUserAvatar(userAvatar);
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
