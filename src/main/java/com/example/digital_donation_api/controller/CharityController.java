package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.CharityMapper;
import com.example.digital_donation_api.dto.mapper.AnnouncementMapper;
import com.example.digital_donation_api.dto.request.CharityCreateRequest;
import com.example.digital_donation_api.dto.response.CharityResponse;
import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.entity.Announcement;
import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.service.CharityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/charities")
@RequiredArgsConstructor
public class CharityController {

    private final CharityService charityService;
    private final AnnouncementRepository announcementRepository;

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
        charity.setCategory(request.getCategory());
        
        Charity createdCharity = charityService.create(charity, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CharityMapper.toResponse(createdCharity));
    }
    
    @PutMapping("/{charityId}")
    public ResponseEntity<CharityResponse> updateCharity(
            @PathVariable Long charityId,
            @Valid @RequestBody CharityCreateRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        
        Charity charityData = new Charity();
        charityData.setName(request.getName());
        charityData.setDescription(request.getDescription());
        charityData.setLogo(request.getLogo());
        charityData.setRegistrationNumber(request.getRegistrationNumber());
        charityData.setAddress(request.getAddress());
        charityData.setContactEmail(request.getContactEmail());
        charityData.setContactPhone(request.getContactPhone());
        charityData.setWebsite(request.getWebsite());
        charityData.setCategory(request.getCategory());
        
        Charity updatedCharity = charityService.update(charityId, charityData, user.getId());
        return ResponseEntity.ok(CharityMapper.toResponse(updatedCharity));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCharities(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Charity> charityPage;
        if ("verified".equalsIgnoreCase(status)) {
            charityPage = charityService.getVerified(pageable);
        } else {
            charityPage = charityService.getAll(pageable);
        }
        
        List<CharityResponse> responses = charityPage.getContent().stream()
                .map(CharityMapper::toResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", responses);
        response.put("currentPage", charityPage.getNumber());
        response.put("totalPages", charityPage.getTotalPages());
        response.put("totalElements", charityPage.getTotalElements());
        response.put("size", charityPage.getSize());
        response.put("hasNext", charityPage.hasNext());
        response.put("hasPrevious", charityPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharityResponse> getCharityById(@PathVariable Long id) {
        Charity charity = charityService.getById(id);
        return ResponseEntity.ok(CharityMapper.toResponse(charity));
    }

    @GetMapping("/{charityId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getCharityAnnouncements(@PathVariable Long charityId) {
        List<Announcement> announcements = announcementRepository.findByCharityIdOrderByCreatedAtDesc(charityId);
        List<AnnouncementResponse> responses = announcements.stream()
                .map(AnnouncementMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
