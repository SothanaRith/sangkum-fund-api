package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.PostMapper;
import com.example.digital_donation_api.dto.response.PostResponse;
import com.example.digital_donation_api.entity.Post;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/blog")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBlogController {

    private final PostService postService;

    // 1. Get all blog articles with pagination
    @GetMapping
    public ResponseEntity<?> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        Page<Post> articlePage = postService.getAll(page, size, status);
        return ResponseEntity.ok(pageResponse(articlePage));
    }

    // 2. Get published articles only
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> publishedPage = postService.getAll(page, size, "PUBLISHED");
        return ResponseEntity.ok(pageResponse(publishedPage));
    }

    // 3. Get draft articles only
    @GetMapping("/drafts")
    public ResponseEntity<?> getDraftArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Post> draftPage = postService.getAll(page, size, "DRAFT");
        return ResponseEntity.ok(pageResponse(draftPage));
    }

    // 4. Get single article by ID
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(PostMapper.toResponse(postService.getById(id)));
    }

    // 5. Get article by slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(PostMapper.toResponse(postService.getBySlugAdmin(slug)));
    }

    // 6. Create new article
    @PostMapping
    public ResponseEntity<PostResponse> createArticle(
            @RequestBody Post article,
            Authentication authentication
    ) {
        User admin = (User) authentication.getPrincipal();
        Post created = postService.create(article, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toResponse(created));
    }

    // 7. Update article
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updateArticle(
            @PathVariable Long id,
            @RequestBody Post articleUpdate
    ) {
        Post existing = postService.getById(id);
        Long authorId = existing.getAuthor() != null ? existing.getAuthor().getId() : null;
        Post updated = postService.update(id, articleUpdate, authorId);
        return ResponseEntity.ok(PostMapper.toResponse(updated));
    }

    // 8. Delete article
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArticle(@PathVariable Long id) {
        Post existing = postService.getById(id);
        Long authorId = existing.getAuthor() != null ? existing.getAuthor().getId() : null;
        postService.delete(id, authorId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Article deleted successfully",
                "id", id
        ));
    }

    // 9. Publish article
    @PostMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(PostMapper.toResponse(postService.publish(id)));
    }

    // 10. Unpublish article
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<PostResponse> unpublishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(PostMapper.toResponse(postService.unpublish(id)));
    }

    // 11. Get blog statistics
    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Long>> getBlogStats() {
        return ResponseEntity.ok(postService.getStats());
    }

    // 12. Get recent articles
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentArticles(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<Post> recent = postService.getRecent(limit);
        return ResponseEntity.ok(Map.of(
                "content", recent.stream().map(PostMapper::toResponse).toList(),
                "count", recent.size()
        ));
    }

    // Helper method to build paginated response
    private Map<String, Object> pageResponse(Page<Post> page) {
        return Map.of(
                "content", page.getContent().stream().map(PostMapper::toResponse).toList(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "pageNumber", page.getNumber()
        );
    }
}
