package com.certifolio.server.domain.mentoring.dto.response;

import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.entity.MentorSkill;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

public class MentorResponseDTO {

    @Builder
    public record MentorsResponse(
            List<MentorListItem> mentors,
            int total
    ) {}

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
                            ? (mentor.getBio().length() > 100 ? mentor.getBio().substring(0, 100) + "..." : mentor.getBio())
                            : null)
                    .build();
        }
    }

    @Builder
    public record MentorProfileResponse(
            Long id,
            String name,
            String title,
            String company,
            String experience,
            List<String> skills,
            String bio,
            List<EducationResponseDTO> education,
            List<CareerResponseDTO> career,
            List<String> achievements,
            List<SpecialtyItem> specialties,
            List<TimeSlotItem> availableSlots,
            String status
    ) {
        public static MentorProfileResponse from(Mentor mentor, List<EducationResponseDTO> education, List<CareerResponseDTO> career) {
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
                            .map(a -> new TimeSlotItem(
                                    a.getDate().toString(),
                                    a.getTime().toString(),
                                    a.getSlotType() != null ? a.getSlotType().name() : null))
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    public record SpecialtyItem(
            String name,
            int level
    ) {}

    public record TimeSlotItem(
            String date,
            String time,
            String type
    ) {}

    @Builder
    public record ApplyMentorResponse(
            boolean success,
            String message,
            Long mentorId
    ) {}
}
