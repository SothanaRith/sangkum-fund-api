package com.example.digital_donation_api.controller;

import com.example.digital_donation_api.dto.mapper.AnnouncementMapper;
import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.entity.Announcement;
import com.example.digital_donation_api.entity.AnnouncementComment;
import com.example.digital_donation_api.entity.AnnouncementReaction;
import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.ReactionType;
import com.example.digital_donation_api.entity.AnnouncementCommentReaction;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.exception.ResourceNotFoundException;
import com.example.digital_donation_api.repository.AnnouncementCommentRepository;
import com.example.digital_donation_api.repository.AnnouncementCommentReactionRepository;
import com.example.digital_donation_api.repository.AnnouncementReactionRepository;
import com.example.digital_donation_api.repository.AnnouncementRepository;
import com.example.digital_donation_api.repository.CharityRepository;
import com.example.digital_donation_api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCommentRepository commentRepository;
    private final AnnouncementReactionRepository reactionRepository;
    private final AnnouncementCommentReactionRepository commentReactionRepository;
    private final EventRepository eventRepository;
    private final CharityRepository charityRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<AnnouncementResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Announcement> announcements = announcementRepository.findAll(pageRequest);
        return ResponseEntity.ok(announcements.map(a -> AnnouncementMapper.toResponse(a, commentRepository, reactionRepository, commentReactionRepository)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnnouncementResponse> create(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Announcement a = new Announcement();
        a.setAuthor(user);
        a.setTitle((String) body.get("title"));
        a.setContent((String) body.get("content"));

        if (body.get("eventId") != null) {
            Long eventId = Long.valueOf(body.get("eventId").toString());
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            a.setEvent(event);
        }
        if (body.get("charityId") != null) {
            Long charityId = Long.valueOf(body.get("charityId").toString());
            Charity charity = charityRepository.findById(charityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Charity not found"));
            a.setCharity(charity);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(AnnouncementMapper.toResponse(announcementRepository.save(a), commentRepository, reactionRepository, commentReactionRepository));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnnouncementResponse> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));
        if (body.containsKey("title"))   a.setTitle(body.get("title"));
        if (body.containsKey("content")) a.setContent(body.get("content"));
        return ResponseEntity.ok(AnnouncementMapper.toResponse(announcementRepository.save(a), commentRepository, reactionRepository, commentReactionRepository));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Reactions ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/reactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        ReactionType type = ReactionType.valueOf(body.getOrDefault("reactionType", "LIKE").toUpperCase());

        if (!reactionRepository.existsByAnnouncementIdAndUserIdAndType(id, user.getId(), type)) {
            AnnouncementReaction reaction = new AnnouncementReaction();
            reaction.setAnnouncement(announcement);
            reaction.setUser(user);
            reaction.setType(type);
            reactionRepository.save(reaction);
        }

        return ResponseEntity.ok(Map.of("success", true, "reactionType", type.name()));
    }

    @DeleteMapping("/{id}/reactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        for (ReactionType type : ReactionType.values()) {
            if (reactionRepository.existsByAnnouncementIdAndUserIdAndType(id, user.getId(), type)) {
                reactionRepository.findAll().stream()
                        .filter(r -> r.getAnnouncement().getId().equals(id)
                                && r.getUser().getId().equals(user.getId())
                                && r.getType() == type)
                        .findFirst()
                        .ifPresent(reactionRepository::delete);
                break;
            }
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Comments ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long id) {
        List<AnnouncementComment> comments = commentRepository.findByAnnouncementIdOrderByCreatedAtAsc(id);
        List<Map<String, Object>> result = comments.stream().map(this::toCommentMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Comment cannot be empty"));
        }

        AnnouncementComment comment = new AnnouncementComment();
        comment.setAnnouncement(announcement);
        comment.setUser(user);
        comment.setContent(content.trim());

        if (body.get("parentId") != null && !body.get("parentId").toString().isBlank()) {
            Long parentId = Long.valueOf(body.get("parentId").toString());
            AnnouncementComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toCommentMap(commentRepository.save(comment)));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        AnnouncementComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Not allowed"));
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Comment Reactions ─────────────────────────────────────────────────────

    @PostMapping("/{id}/comments/{commentId}/reactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> addCommentReaction(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        AnnouncementComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getAnnouncement().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Comment does not belong to announcement"));
        }

        ReactionType type = ReactionType.valueOf(body.getOrDefault("reactionType", "LIKE").toUpperCase());

        AnnouncementCommentReaction reaction = commentReactionRepository.findByCommentIdAndUserId(commentId, user.getId())
                .orElse(new AnnouncementCommentReaction());

        reaction.setComment(comment);
        reaction.setUser(user);
        reaction.setType(type);
        commentReactionRepository.save(reaction);

        return ResponseEntity.ok(Map.of("success", true, "reactionType", type.name()));
    }

    @DeleteMapping("/{id}/comments/{commentId}/reactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> removeCommentReaction(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        commentReactionRepository.findByCommentIdAndUserId(commentId, user.getId())
                .ifPresent(commentReactionRepository::delete);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> toCommentMap(AnnouncementComment c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("content", c.getContent());
        m.put("userId", c.getUser().getId());
        m.put("userName", c.getUser().getName());
        m.put("userAvatar", c.getUser().getAvatar());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }
}
