package com.example.digital_donation_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String slug;

    @Size(max = 500)
    private String excerpt;

    @NotBlank
    private String content;

    private String coverImageUrl;

    private List<String> tags;

    private String status; // DRAFT or PUBLISHED

    private Boolean featured;
}
