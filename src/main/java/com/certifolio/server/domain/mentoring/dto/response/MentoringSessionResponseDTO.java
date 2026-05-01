package com.certifolio.server.domain.mentoring.dto.response;

import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.entity.MentoringSession;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

public class MentoringSessionResponseDTO {

    @Builder
    public record SessionsResponse(
            List<SessionItem> sessions,
            int total
    ) {}

    @Builder
    public record SessionItem(
            Long id,
            Long mentorId,
            SessionMentor mentor,
            String status,
            String topic,
            String startDate
    ) {
        public static SessionItem from(MentoringSession session) {
            return SessionItem.builder()
                    .id(session.getId())
                    .mentorId(session.getMentor().getId())
                    .mentor(SessionMentor.from(session.getMentor()))
                    .status(session.getStatus().name().toLowerCase())
                    .topic(session.getTopic())
                    .startDate(session.getStartDate() != null ? session.getStartDate().toString() : null)
                    .build();
        }
    }

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

    @Builder
    public record UpdateSessionResponse(
            boolean success,
            String message
    ) {}
}
