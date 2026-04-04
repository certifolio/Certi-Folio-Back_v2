package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentorSkill;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 멘토 관련 DTO (Java Record)
 */
public class MentorDTO {

    /**
     * 멘토 목록 응답
     */
    @Builder
    public record MentorsResponse(
            List<MentorListItem> mentors,
            int total
    ) {}

    /**
     * 멘토 목록 아이템
     */
    @Builder
    public record MentorListItem(
            Long id,
            String name,
            String title,
            String company,
            String experience,
            List<String> skills,
            String description
    ) {
        public static MentorListItem from(Mentor mentor) {
            return MentorListItem.builder()
                    .id(mentor.getId())
                    .name(mentor.getName())
                    .title(mentor.getTitle())
                    .company(mentor.getCompany())
                    .experience(mentor.getExperience())
                    .skills(mentor.getSkills().stream()
                            .map(MentorSkill::getSkillName)
                            .collect(Collectors.toList()))
                    .description(mentor.getBio() != null
                            ? (mentor.getBio().length() > 100 ? mentor.getBio().substring(0, 100) + "..."
                                    : mentor.getBio())
                            : null)
                    .build();
        }
    }

    /**
     * 멘토 프로필 상세 응답
     */
    @Builder
    public record MentorProfileResponse(
            Long id,
            String name,
            String title,
            String company,
            String experience,
            List<String> skills,
            String bio,
            List<EducationDTO> education,
            List<CareerDTO> career,
            List<String> achievements,
            List<SpecialtyItem> specialties,
            List<TimeSlotItem> availableSlots,
            String status
    ) {
        public static MentorProfileResponse from(Mentor mentor, List<EducationDTO> education, List<CareerDTO> career) {
            return MentorProfileResponse.builder()
                    .id(mentor.getId())
                    .name(mentor.getName())
                    .title(mentor.getTitle())
                    .company(mentor.getCompany())
                    .experience(mentor.getExperience())
                    .status(mentor.getStatus() != null ? mentor.getStatus().name() : null)
                    .skills(mentor.getSkills().stream()
                            .map(MentorSkill::getSkillName)
                            .collect(Collectors.toList()))
                    .bio(mentor.getBio())
                    .education(education)
                    .career(career)
                    .achievements(List.of())
                    .specialties(mentor.getSkills().stream()
                            .map(s -> new SpecialtyItem(s.getSkillName(), s.getLevel() != null ? s.getLevel() : 3))
                            .collect(Collectors.toList()))
                    .availableSlots(mentor.getAvailabilities().stream()
                            .map(a -> new TimeSlotItem(a.getDate(), a.getTime(), a.getSlotType()))
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    /**
     * 전문 분야 아이템
     */
    public record SpecialtyItem(
            String name,
            int level
    ) {}

    /**
     * 가용 시간 아이템
     */
    public record TimeSlotItem(
            String date,
            String time,
            String type
    ) {}

    /**
     * 멘토 신청 요청
     */
    public record MentorApplicationRequest(
            String name,
            String title,
            String company,
            String experience,
            List<String> expertise,
            String bio,
            List<String> availability,
            String preferredFormat,
            List<String> certificates
    ) {}

    /**
     * 멘토 신청 응답
     */
    @Builder
    public record ApplyMentorResponse(
            boolean success,
            String message,
            Long mentorId
    ) {}
}
