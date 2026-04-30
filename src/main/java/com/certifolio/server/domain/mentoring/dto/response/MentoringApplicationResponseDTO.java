package com.certifolio.server.domain.mentoring.dto.response;

import com.certifolio.server.domain.mentoring.entity.MentoringApplication;
import lombok.Builder;

import java.util.List;

public class MentoringApplicationResponseDTO {

    @Builder
    public record CreateResponse(
            boolean success,
            String message,
            Long applicationId
    ) {}

    @Builder
    public record ApplicationsResponse(
            List<ApplicationItem> applications,
            int total
    ) {}

    @Builder
    public record ApplicationItem(
            Long id,
            String menteeName,
            String menteeImage,
            Long menteeUserId,
            String mentorName,
            Long mentorId,
            String topic,
            String description,
            String status,
            String createdAt
    ) {
        public static ApplicationItem from(MentoringApplication app) {
            return ApplicationItem.builder()
                    .id(app.getId())
                    .menteeName(app.getMenteeName())
                    .menteeImage(app.getMentee().getPicture())
                    .menteeUserId(app.getMentee().getId())
                    .mentorName(app.getMentor().getName())
                    .mentorId(app.getMentor().getId())
                    .topic(app.getTopic())
                    .description(app.getDescription())
                    .status(app.getStatus().name().toLowerCase())
                    .createdAt(app.getCreatedAt() != null ? app.getCreatedAt().toString() : null)
                    .build();
        }
    }

    @Builder
    public record ActionResponse(
            boolean success,
            String message,
            Long sessionId
    ) {}
}
