package com.example.digital_donation_api.dto.mapper;

import com.example.digital_donation_api.dto.response.AnnouncementResponse;
import com.example.digital_donation_api.entity.Announcement;

import com.example.digital_donation_api.repository.AnnouncementCommentRepository;
import com.example.digital_donation_api.repository.AnnouncementCommentReactionRepository;
import com.example.digital_donation_api.repository.AnnouncementReactionRepository;
import java.util.List;
import java.util.stream.Collectors;

public class AnnouncementMapper {
    public static AnnouncementResponse toResponse(Announcement a) {
        return toResponse(a, null, null, null);
    }

    public static AnnouncementResponse toResponse(
            Announcement a,
            AnnouncementCommentRepository commentRepo,
            AnnouncementReactionRepository reactionRepo,
            AnnouncementCommentReactionRepository commentReactionRepo
    ) {
        AnnouncementResponse.AuthorDto author = a.getAuthor() != null ? new AnnouncementResponse.AuthorDto(a.getAuthor().getId(), a.getAuthor().getName()) : null;
        AnnouncementResponse.EventDto event = a.getEvent() != null ? new AnnouncementResponse.EventDto(a.getEvent().getId(), a.getEvent().getTitle()) : null;
        AnnouncementResponse.CharityDto charity = a.getCharity() != null ? new AnnouncementResponse.CharityDto(a.getCharity().getId(), a.getCharity().getName()) : null;

        List<AnnouncementResponse.CommentDto> comments = null;
        List<AnnouncementResponse.ReactionDto> reactions = null;

        if (commentRepo != null) {
            java.util.List<com.example.digital_donation_api.entity.AnnouncementComment> allComments = commentRepo.findByAnnouncementIdOrderByCreatedAtAsc(a.getId());
            comments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .map(c -> {
                    return new AnnouncementResponse.CommentDto(
                            c.getId(),
                            c.getContent(),
                            c.getUser().getId(),
                            c.getUser().getName(),
                            c.getUser().getAvatar(),
                            null,
                            c.getCreatedAt(),
                            commentReactionRepo != null ? commentReactionRepo.findByCommentId(c.getId()).stream().map(cr -> new AnnouncementResponse.ReactionDto(
                                cr.getId(), cr.getType().name(), cr.getUser().getId()
                            )).collect(Collectors.toList()) : null,
                            allComments.stream()
                                .filter(r -> r.getParent() != null && r.getParent().getId().equals(c.getId()))
                                .map(r -> new AnnouncementResponse.CommentDto(
                                    r.getId(),
                                    r.getContent(),
                                    r.getUser().getId(),
                                    r.getUser().getName(),
                                    r.getUser().getAvatar(),
                                    c.getId(),
                                    r.getCreatedAt(),
                                    commentReactionRepo != null ? commentReactionRepo.findByCommentId(r.getId()).stream().map(cr -> new AnnouncementResponse.ReactionDto(
                                        cr.getId(), cr.getType().name(), cr.getUser().getId()
                                    )).collect(Collectors.toList()) : null,
                                    null
                                )).collect(Collectors.toList())
                    );
                }).collect(Collectors.toList());
        }

        if (reactionRepo != null) {
            reactions = reactionRepo.findByAnnouncementId(a.getId()).stream().map(r -> {
                return new AnnouncementResponse.ReactionDto(
                        r.getId(),
                        r.getType().name(),
                        r.getUser().getId()
                );
            }).collect(Collectors.toList());
        }

        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                author,
                event,
                charity,
                comments,
                reactions,
                a.getCreatedAt()
        );
    }
}
