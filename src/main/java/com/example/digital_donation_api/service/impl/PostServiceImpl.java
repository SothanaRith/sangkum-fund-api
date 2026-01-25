package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Post;
import com.example.digital_donation_api.entity.PostStatus;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.repository.PostRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Post create(Post post, Long authorId) {
        User author = userRepository.findById(authorId).orElseThrow();
        post.setAuthor(author);

        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(generateSlug(post.getTitle()));
        }

        if (postRepository.existsBySlug(post.getSlug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        if (post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }

        return postRepository.save(post);
    }

    @Override
    public Post update(Long id, Post postData, Long authorId) {
        Post existing = postRepository.findById(id).orElseThrow();

        // Basic author/admin ownership check can be extended as needed
        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Not allowed to modify this post");
        }

        existing.setTitle(postData.getTitle());
        existing.setExcerpt(postData.getExcerpt());
        existing.setContent(postData.getContent());
        existing.setCoverImageUrl(postData.getCoverImageUrl());
        existing.setTags(postData.getTags());
        existing.setFeatured(postData.getFeatured() != null ? postData.getFeatured() : existing.getFeatured());

        if (postData.getSlug() != null && !postData.getSlug().isBlank() && !postData.getSlug().equals(existing.getSlug())) {
            if (postRepository.existsBySlug(postData.getSlug())) {
                throw new IllegalArgumentException("Slug already exists");
            }
            existing.setSlug(postData.getSlug());
        }

        if (postData.getStatus() != null) {
            existing.setStatus(postData.getStatus());
            if (postData.getStatus() == PostStatus.PUBLISHED && existing.getPublishedAt() == null) {
                existing.setPublishedAt(LocalDateTime.now());
            }
        }

        return postRepository.save(existing);
    }

    @Override
    public void delete(Long id, Long authorId) {
        Post existing = postRepository.findById(id).orElseThrow();
        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Not allowed to delete this post");
        }
        postRepository.delete(existing);
    }

    @Override
    public Page<Post> getPublished(String search, String tag, Pageable pageable) {
        String s = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;
        String t = (tag != null && !tag.isBlank()) ? tag.trim().toLowerCase() : null;
        return postRepository.searchPublished(PostStatus.PUBLISHED, s, t, pageable);
    }

    @Override
    public Page<Post> getFeatured(Pageable pageable) {
        return postRepository.findByStatusAndFeaturedTrueOrderByPublishedAtDesc(PostStatus.PUBLISHED, pageable);
    }

    @Override
    public Optional<Post> getPublishedBySlug(String slug) {
        return postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED);
    }

    @Override
    public Optional<Post> getBySlug(String slug) {
        return postRepository.findBySlug(slug);
    }

    private String generateSlug(String input) {
        String nowhitespace = input == null ? "" : input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^a-zA-Z0-9-]").matcher(normalized).replaceAll("");
        slug = slug.replaceAll("-+", "-").toLowerCase(Locale.ROOT);
        if (slug.isBlank()) {
            slug = "post-" + System.currentTimeMillis();
        }
        // ensure uniqueness by appending timestamp if exists
        if (postRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        return slug;
    }
}
