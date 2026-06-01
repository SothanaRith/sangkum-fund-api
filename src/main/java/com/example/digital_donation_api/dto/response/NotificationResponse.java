package com.example.digital_donation_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String data;
    private String actionUrl;
    private Long relatedId;
    private Boolean isRead;
    private String createdAt;
}
