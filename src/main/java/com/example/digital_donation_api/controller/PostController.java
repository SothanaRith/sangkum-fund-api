package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.PostMapper;
import com.example.digital_donation_api.dto.request.PostRequest;
import com.example.digital_donation_api.dto.response.PostResponse;
import com.example.digital_donation_api.entity.Post;
import com.example.digital_donation_api.entity.PostStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setTags(PostMapper.tagsToString(request.getTags()));
        post.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);

        if (request.getStatus() != null && request.getStatus().equalsIgnoreCase("PUBLISHED")) {
            post.setStatus(PostStatus.PUBLISHED);
        } else {
            post.setStatus(PostStatus.DRAFT);
        }

        Post created = postService.create(post, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setTags(PostMapper.tagsToString(request.getTags()));
        post.setFeatured(request.getFeatured());

        if (request.getStatus() != null) {
            post.setStatus("PUBLISHED".equalsIgnoreCase(request.getStatus()) ? PostStatus.PUBLISHED : PostStatus.DRAFT);
        }

        Post updated = postService.update(id, post, user.getId());
        return ResponseEntity.ok(PostMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        String status = payload.get("status") != null ? payload.get("status").toString() : null;
        Boolean featured = payload.get("featured") != null ? Boolean.valueOf(payload.get("featured").toString()) : null;

        Post post = new Post();
        if (status != null) {
            post.setStatus("PUBLISHED".equalsIgnoreCase(status) ? PostStatus.PUBLISHED : PostStatus.DRAFT);
        }
        post.setFeatured(featured);

        Post updated = postService.update(id, post, user.getId());
        return ResponseEntity.ok(PostMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        postService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage = postService.getPublished(search, tag, pageable);
        var content = postPage.getContent().stream().map(PostMapper::toResponse).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("currentPage", postPage.getNumber());
        response.put("totalPages", postPage.getTotalPages());
        response.put("totalElements", postPage.getTotalElements());
        response.put("size", postPage.getSize());
        response.put("hasNext", postPage.hasNext());
        response.put("hasPrevious", postPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<?> featured(
            @RequestParam(defaultValue = "3") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("publishedAt").descending());
        Page<Post> postPage = postService.getFeatured(pageable);
        var content = postPage.getContent().stream().map(PostMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug, @RequestParam(defaultValue = "false") boolean includeDraft, Authentication authentication) {
        if (includeDraft) {
            if (authentication == null || authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden"));
            }
            return postService.getBySlug(slug)
                    .map(post -> ResponseEntity.ok(PostMapper.toResponse(post)))
                    .orElse(ResponseEntity.notFound().build());
        }

        return postService.getPublishedBySlug(slug)
                .map(post -> ResponseEntity.ok(PostMapper.toResponse(post)))
                .orElse(ResponseEntity.notFound().build());
    }
}
