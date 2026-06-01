package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnnouncementCreateRequest {

    @NotBlank
    @jakarta.validation.constraints.NotBlank
    private String title;

    @NotBlank
    @jakarta.validation.constraints.NotBlank
    private String content;

    private Long eventId;
    private Long charityId;
}
