package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JoinEventRequest {
    @NotNull
    private Long eventId;
}
