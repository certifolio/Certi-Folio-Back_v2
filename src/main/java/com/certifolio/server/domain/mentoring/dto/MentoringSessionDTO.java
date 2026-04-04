package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentoringSession;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 멘토링 세션 관련 DTO (Java Record)
 */
public class MentoringSessionDTO {

    /**
     * 세션 목록 응답
     */
    @Builder
    public record SessionsResponse(
            List<SessionItem> sessions,
            int total
    ) {}

    /**
     * 세션 아이템
     */
    @Builder
    public record SessionItem(
            Long id,
            SessionMentor mentor,
            String status,
            String topic,
            String startDate
    ) {
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
    @Builder
    public record SessionMentor(
            String name,
            String title,
            String company,
            List<String> expertise
    ) {
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
    public record CreateSessionRequest(
            Long mentorId,
            Long requestId,
            String topic
    ) {}

    /**
     * 세션 상태 업데이트 요청
     */
    public record UpdateSessionStatusRequest(
            String status
    ) {}

    /**
     * 세션 업데이트 응답
     */
    @Builder
    public record UpdateSessionResponse(
            boolean success,
            String message
    ) {}
}
