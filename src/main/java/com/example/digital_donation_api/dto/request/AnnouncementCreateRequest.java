package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnnouncementCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Long eventId;
    private Long charityId;
}
