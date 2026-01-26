package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Post;
import com.example.digital_donation_api.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlugAndStatus(String slug, PostStatus status);

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT p FROM Post p WHERE p.status = :status " +
            "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:tag IS NULL OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%')))")
    Page<Post> searchPublished(
            @Param("status") PostStatus status,
            @Param("search") String search,
            @Param("tag") String tag,
            Pageable pageable
    );

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndFeaturedTrueOrderByPublishedAtDesc(PostStatus status, Pageable pageable);

    // Admin-specific query methods
    long countByStatus(PostStatus status);

    long countByFeatured(Boolean featured);
}
