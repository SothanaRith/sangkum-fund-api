package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.BusinessCardMapper;
import com.example.digital_donation_api.dto.request.BusinessCardCreateRequest;
import com.example.digital_donation_api.dto.response.BusinessCardResponse;
import com.example.digital_donation_api.entity.BusinessCard;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.BusinessCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-cards")
@RequiredArgsConstructor
public class BusinessCardController {

    private final BusinessCardRepository businessCardRepository;

    @GetMapping("/my-card")
    public ResponseEntity<BusinessCardResponse> getMyCard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<BusinessCard> card = businessCardRepository.findByUserId(user.getId());
        
        if (card.isEmpty()) {
            // Return empty response or create a default one
            return ResponseEntity.ok(null);
        }
        
        return ResponseEntity.ok(BusinessCardMapper.toResponse(card.get()));
    }

    @PostMapping
    public ResponseEntity<BusinessCardResponse> createOrUpdateCard(
            @RequestBody BusinessCardCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        
        // Find existing card or create new one
        Optional<BusinessCard> existingCard = businessCardRepository.findByUserId(user.getId());
        BusinessCard card;
        
        if (existingCard.isPresent()) {
            card = existingCard.get();
        } else {
            card = new BusinessCard();
            card.setUser(user);
            // Generate unique slug
            card.setShareSlug(UUID.randomUUID().toString().substring(0, 8));
        }
        
        // Update fields
        card.setTemplate(request.getTemplate());
        card.setTitle(request.getTitle());
        card.setBio(request.getBio());
        card.setContactInfo(request.getContactInfo());
        
        BusinessCard savedCard = businessCardRepository.save(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(BusinessCardMapper.toResponse(savedCard));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BusinessCardResponse> getCardBySlug(@PathVariable String slug) {
        Optional<BusinessCard> card = businessCardRepository.findByShareSlug(slug);
        
        if (card.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(BusinessCardMapper.toResponse(card.get()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyCard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        businessCardRepository.deleteById(user.getId());
        return ResponseEntity.noContent().build();
    }
}
