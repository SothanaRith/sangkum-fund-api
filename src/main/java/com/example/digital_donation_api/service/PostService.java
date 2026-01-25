package com.example.digital_donation_api.service;

import com.example.digital_donation_api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostService {

    Post create(Post post, Long authorId);

    Post update(Long id, Post post, Long authorId);

    void delete(Long id, Long authorId);

    Page<Post> getPublished(String search, String tag, Pageable pageable);

    Page<Post> getFeatured(Pageable pageable);

    Optional<Post> getPublishedBySlug(String slug);

    Optional<Post> getBySlug(String slug);
}
