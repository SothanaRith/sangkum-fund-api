package com.example.digital_donation_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.digital_donation_api.entity.Post;
import com.example.digital_donation_api.entity.PostStatus;
import com.example.digital_donation_api.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/blog")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {
    
    private final PostRepository postRepository;
    
    @Autowired
    public AdminBlogController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
    
    // 1. Get all blog articles with pagination
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllArticles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> articlePage;
            
            if (status != null && !status.isEmpty()) {
                PostStatus postStatus = status.equals("published") ? PostStatus.PUBLISHED : PostStatus.DRAFT;
                articlePage = postRepository.findByStatus(postStatus, pageable);
            } else {
                articlePage = postRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(mapArticles(articlePage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 2. Get published articles only
    @GetMapping("/published")
    public ResponseEntity<Map<String, Object>> getPublishedArticles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> publishedPage = postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
            
            return ResponseEntity.ok(mapArticles(publishedPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 3. Get draft articles only
    @GetMapping("/drafts")
    public ResponseEntity<Map<String, Object>> getDraftArticles(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Post> draftPage = postRepository.findByStatus(PostStatus.DRAFT, pageable);
            
            return ResponseEntity.ok(mapArticles(draftPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 4. Get single article by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getArticle(@PathVariable Long id) {
        try {
            Post article = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", article
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 5. Get article by slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getArticleBySlug(@PathVariable String slug) {
        try {
            Post article = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", article
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 6. Create new article
    @PostMapping
    public ResponseEntity<Map<String, Object>> createArticle(@jakarta.validation.Valid @RequestBody Post article) {
        try {
            if (article.getTitle() == null || article.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Title is required"
                ));
            }
            
            article.setCreatedAt(LocalDateTime.now());
            article.setUpdatedAt(LocalDateTime.now());
            
            // Generate slug if not provided
            if (article.getSlug() == null || article.getSlug().isEmpty()) {
                article.setSlug(generateSlug(article.getTitle()));
            }
            
            Post saved = postRepository.save(article);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Article created successfully",
                "data", saved,
                "id", saved.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 7. Update article
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateArticle(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody Post articleUpdate
    ) {
        try {
            Post article = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            if (articleUpdate.getTitle() != null) {
                article.setTitle(articleUpdate.getTitle());
            }
            if (articleUpdate.getSlug() != null) {
                article.setSlug(articleUpdate.getSlug());
            }
            if (articleUpdate.getExcerpt() != null) {
                article.setExcerpt(articleUpdate.getExcerpt());
            }
            if (articleUpdate.getContent() != null) {
                article.setContent(articleUpdate.getContent());
            }
            if (articleUpdate.getCoverImageUrl() != null) {
                article.setCoverImageUrl(articleUpdate.getCoverImageUrl());
            }
            if (articleUpdate.getAuthor() != null) {
                article.setAuthor(articleUpdate.getAuthor());
            }
            if (articleUpdate.getStatus() != null) {
                article.setStatus(articleUpdate.getStatus());
            }
            if (articleUpdate.getTags() != null) {
                article.setTags(articleUpdate.getTags());
            }
            if (articleUpdate.getFeatured() != null) {
                article.setFeatured(articleUpdate.getFeatured());
            }
            
            article.setUpdatedAt(LocalDateTime.now());
            
            Post updated = postRepository.save(article);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Article updated successfully",
                "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 8. Delete article
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArticle(@PathVariable Long id) {
        try {
            if (!postRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", "Article not found"));
            }
            
            postRepository.deleteById(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Article deleted successfully",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 9. Publish article
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishArticle(@PathVariable Long id) {
        try {
            Post article = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            article.setStatus(PostStatus.PUBLISHED);
            article.setPublishedAt(LocalDateTime.now());
            article.setUpdatedAt(LocalDateTime.now());
            
            Post updated = postRepository.save(article);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Article published successfully",
                "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 10. Unpublish article
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishArticle(@PathVariable Long id) {
        try {
            Post article = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
            
            article.setStatus(PostStatus.DRAFT);
            article.setUpdatedAt(LocalDateTime.now());
            
            Post updated = postRepository.save(article);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Article unpublished successfully",
                "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 11. Get blog statistics
    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getBlogStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("success", true);
            stats.put("totalArticles", postRepository.count());
            stats.put("publishedCount", postRepository.countByStatus(PostStatus.PUBLISHED));
            stats.put("draftCount", postRepository.countByStatus(PostStatus.DRAFT));
            stats.put("featuredCount", postRepository.countByFeatured(true));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // 12. Get recent articles
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentArticles(
        @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            List<Post> recent = postRepository.findAll(Sort.by("createdAt").descending())
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "content", recent,
                "count", recent.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // Helper method to map article page response
    private Map<String, Object> mapArticles(Page<Post> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("content", page.getContent());
        response.put("pageNumber", page.getNumber());
        response.put("pageSize", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }
    
    // Helper method to generate slug from title
    private String generateSlug(String title) {
        return title
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    }
}
