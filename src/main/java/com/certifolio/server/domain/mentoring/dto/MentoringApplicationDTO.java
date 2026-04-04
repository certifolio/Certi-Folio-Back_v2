package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.MentoringApplication;
import lombok.Builder;

import java.util.List;

/**
 * 멘토링 신청 관련 DTO (Java Record)
 */
public class MentoringApplicationDTO {

    /**
     * 신청 생성 요청
     */
    public record CreateRequest(
            Long mentorId,
            String topic,
            String description
    ) {}

    /**
     * 신청 생성 응답
     */
    @Builder
    public record CreateResponse(
            boolean success,
            String message,
            Long applicationId
    ) {}

    /**
     * 신청 목록 응답
     */
    @Builder
    public record ApplicationsResponse(
            List<ApplicationItem> applications,
            int total
    ) {}

    /**
     * 신청 아이템
     */
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

    /**
     * 승인/거절 응답
     */
    @Builder
    public record ActionResponse(
            boolean success,
            String message,
            Long sessionId
    ) {}

    /**
     * 거절 요청
     */
    public record RejectRequest(
            String reason
    ) {}
}
