package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentoringSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 멘토링 세션 관련 DTO 모음
 */
public class MentoringSessionDTO {

    /**
     * 세션 목록 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionsResponse {
        private List<SessionItem> sessions;
        private int total;
    }

    /**
     * 세션 아이템
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionItem {
        private Long id;
        private SessionMentor mentor;
        private String status;
        private String topic;
        private String startDate;

        public static SessionItem from(MentoringSession session) {
            return SessionItem.builder()
                    .id(session.getId())
                    .mentor(SessionMentor.from(session.getMentor()))
                    .status(session.getStatus().name().toLowerCase())
                    .topic(session.getTopic())
                    .startDate(session.getStartDate() != null ? session.getStartDate().toString() : null)
                    .build();
        }
    }

    /**
     * 세션 내 멘토 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionMentor {
        private String name;
        private String title;
        private String company;
        private List<String> expertise;

        public static SessionMentor from(Mentor mentor) {
            return SessionMentor.builder()
                    .name(mentor.getName())
                    .title(mentor.getTitle())
                    .company(mentor.getCompany())
                    .expertise(mentor.getSkills().stream()
                            .map(s -> s.getSkillName())
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    /**
     * 세션 생성 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        private Long mentorId;
        private Long requestId; // 선택적
        private String topic;
    }

    /**
     * 세션 상태 업데이트 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionStatusRequest {
        private String status; // active, completed, cancelled
    }

    /**
     * 세션 업데이트 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionResponse {
        private boolean success;
        private String message;
    }
}
