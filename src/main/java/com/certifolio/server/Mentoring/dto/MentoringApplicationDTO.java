package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.MentoringApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 멘토링 신청 관련 DTO
 */
public class MentoringApplicationDTO {

    /**
     * 신청 생성 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long mentorId;
        private String topic;
        private String description;
    }

    /**
     * 신청 생성 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateResponse {
        private boolean success;
        private String message;
        private Long applicationId;
    }

    /**
     * 신청 목록 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationsResponse {
        private List<ApplicationItem> applications;
        private int total;
    }

    /**
     * 신청 아이템
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationItem {
        private Long id;
        private String menteeName;
        private String menteeImage;
        private Long menteeUserId;
        private String mentorName;
        private Long mentorId;
        private String topic;
        private String description;
        private String status;
        private String createdAt;

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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionResponse {
        private boolean success;
        private String message;
        private Long sessionId; // 승인 시 생성된 세션 ID
    }

    /**
     * 거절 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectRequest {
        private String reason;
    }
}
