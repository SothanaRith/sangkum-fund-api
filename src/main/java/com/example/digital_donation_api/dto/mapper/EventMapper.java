package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.EventImageResponse;
import com.example.digital_donation_api.dto.response.EventResponse;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.repository.EventMemberRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

public class EventMapper {
    public static EventResponse toResponse(Event event) {
        return toResponse(event, null);
    }
    
    public static EventResponse toResponse(Event event, EventMemberRepository eventMemberRepository) {
        // Calculate progress percentage
        Double progressPercentage = 0.0;
        if (event.getGoalAmount() != null && event.getGoalAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = event.getCurrentAmount()
                    .divide(event.getGoalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        
        // Get participant count if repository is provided
        Integer participantCount = 0;
        if (eventMemberRepository != null) {
            participantCount = (int) eventMemberRepository.countByEventId(event.getId());
        }
        
        // Convert relative avatar URLs to absolute
        String ownerAvatar = event.getOwner().getAvatar();
        if (ownerAvatar != null && ownerAvatar.startsWith("/uploads/")) {
            ownerAvatar = "http://localhost:8080" + ownerAvatar;
        }
        
        String charityLogo = event.getCharity() != null ? event.getCharity().getLogo() : null;
        if (charityLogo != null && charityLogo.startsWith("/uploads/")) {
            charityLogo = "http://localhost:8080" + charityLogo;
        }
        
        // Map event images
        var images = event.getImages().stream()
                .map(img -> new EventImageResponse(
                        img.getId(),
                        img.getImageUrl(),
                        img.getIsPrimary(),
                        img.getDisplayOrder()
                ))
                .collect(Collectors.toList());
        
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getImageUrl(),
                event.getGoalAmount(),
                event.getCurrentAmount(),
                event.getStatus().name(),
                event.getVisibility().name(),
                event.getStartDate(),
                event.getEndDate(),
                event.getOwner().getId(),
                event.getOwner().getName(),
                ownerAvatar,
                event.getCharity() != null ? event.getCharity().getId() : null,
                event.getCharity() != null ? event.getCharity().getName() : null,
                charityLogo,
                0, // TODO: Get actual donation count from repository
                participantCount,
                progressPercentage,
                images,
                event.getLocation(),
                event.getLatitude(),
                event.getLongitude(),
                event.getCategory()
        );
    }
}
