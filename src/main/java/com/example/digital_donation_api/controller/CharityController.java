package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.CharityMapper;
import com.example.digital_donation_api.dto.request.CharityCreateRequest;
import com.example.digital_donation_api.dto.response.CharityResponse;
import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.CharityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/charities")
@RequiredArgsConstructor
public class CharityController {

    private final CharityService charityService;

    @PostMapping
    public ResponseEntity<CharityResponse> createCharity(
            @Valid @RequestBody CharityCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        
        Charity charity = new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setLogo(request.getLogo());
        charity.setRegistrationNumber(request.getRegistrationNumber());
        charity.setAddress(request.getAddress());
        charity.setContactEmail(request.getContactEmail());
        charity.setContactPhone(request.getContactPhone());
        charity.setWebsite(request.getWebsite());
        
        Charity createdCharity = charityService.create(charity, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CharityMapper.toResponse(createdCharity));
    }

    @GetMapping
    public ResponseEntity<List<CharityResponse>> getAllCharities(
            @RequestParam(required = false) String status
    ) {
        List<Charity> charities;
        
        if ("verified".equalsIgnoreCase(status)) {
            charities = charityService.getVerified();
        } else {
            charities = charityService.getAll();
        }
        
        List<CharityResponse> responses = charities.stream()
                .map(CharityMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharityResponse> getCharityById(@PathVariable Long id) {
        Charity charity = charityService.getById(id);
        return ResponseEntity.ok(CharityMapper.toResponse(charity));
    }
}
