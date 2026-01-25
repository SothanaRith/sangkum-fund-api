package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.PostResponse;
import com.example.digital_donation_api.entity.Post;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PostMapper {

    public static PostResponse toResponse(Post post) {
        List<String> tags = parseTags(post.getTags());

        String authorAvatar = post.getAuthor() != null ? post.getAuthor().getAvatar() : null;
        if (authorAvatar != null && authorAvatar.startsWith("/uploads/")) {
            authorAvatar = "http://localhost:8080" + authorAvatar;
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getCoverImageUrl(),
                tags,
                post.getStatus().name(),
                post.getFeatured(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getName() : null,
                authorAvatar
        );
    }

    public static String tagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase())
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    public static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
