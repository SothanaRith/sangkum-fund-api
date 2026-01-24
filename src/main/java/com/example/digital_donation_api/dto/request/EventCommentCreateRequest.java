package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventCommentCreateRequest {
    
    @NotBlank(message = "Comment content is required")
    private String content;
}
