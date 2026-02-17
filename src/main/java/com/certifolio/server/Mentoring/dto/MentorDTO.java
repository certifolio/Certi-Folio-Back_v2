package com.certifolio.server.Mentoring.dto;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentorAvailability;
import com.certifolio.server.Mentoring.domain.MentorReview;
import com.certifolio.server.Mentoring.domain.MentorSkill;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 멘토 관련 DTO 모음
 */
public class MentorDTO {

    /**
     * 멘토 목록 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorsResponse {
        private List<MentorListItem> mentors;
        private int total;
    }

    /**
     * 멘토 목록 아이템
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorListItem {
        private Long id;
        private String name;
        private String title;
        private String company;
        private String experience;
        private Double rating;
        private Integer reviews;
        private List<String> skills;
        private String location;
        private String price;
        private String image;
        private boolean verified;
        private String description;

        public static MentorListItem from(Mentor mentor) {
            return MentorListItem.builder()
                    .id(mentor.getId())
                    .name(mentor.getName())
                    .title(mentor.getTitle())
                    .company(mentor.getCompany())
                    .experience(mentor.getExperience())
                    .rating(mentor.getRating())
                    .reviews(mentor.getReviewCount())
                    .skills(mentor.getSkills().stream()
                            .map(MentorSkill::getSkillName)
                            .collect(Collectors.toList()))
                    .location(mentor.getLocation())
                    .price(mentor.getPrice())
                    .image(mentor.getProfileImage())
                    .verified(mentor.isVerified())
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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorProfileResponse {
        private Long id;
        private String name;
        private String title;
        private String company;
        private String experience;
        private Double rating;
        private Integer reviews;
        private List<String> skills;
        private String location;
        private String price;
        private String image;
        private boolean verified;
        private String bio;
        private List<EducationDTO> education;
        private List<CareerDTO> career;
        private List<String> achievements;
        private List<SpecialtyItem> specialties;
        private List<TimeSlotItem> availableSlots;
        private List<ReviewItem> reviewList;
        private ResponseStats responseStats;

        public static MentorProfileResponse from(Mentor mentor, List<EducationDTO> education, List<CareerDTO> career) {
            return MentorProfileResponse.builder()
                    .id(mentor.getId())
                    .name(mentor.getName())
                    .title(mentor.getTitle())
                    .company(mentor.getCompany())
                    .experience(mentor.getExperience())
                    .rating(mentor.getRating())
                    .reviews(mentor.getReviewCount())
                    .skills(mentor.getSkills().stream()
                            .map(MentorSkill::getSkillName)
                            .collect(Collectors.toList()))
                    .location(mentor.getLocation())
                    .price(mentor.getPrice())
                    .image(mentor.getProfileImage())
                    .verified(mentor.isVerified())
                    .bio(mentor.getBio())
                    .education(education)
                    .career(career)
                    .achievements(List.of()) // 추후 확장
                    .specialties(mentor.getSkills().stream()
                            .map(s -> new SpecialtyItem(s.getSkillName(), s.getLevel() != null ? s.getLevel() : 3))
                            .collect(Collectors.toList()))
                    .availableSlots(mentor.getAvailabilities().stream()
                            .map(a -> new TimeSlotItem(a.getDate(), a.getTime(), a.getSlotType()))
                            .collect(Collectors.toList()))
                    .reviewList(mentor.getReviews().stream()
                            .map(ReviewItem::from)
                            .collect(Collectors.toList()))
                    .responseStats(ResponseStats.builder()
                            .averageResponseTime("24시간 이내")
                            .responseRate(95)
                            .repeatRate(80)
                            .totalMentoring(mentor.getReviewCount())
                            .build())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialtyItem {
        private String name;
        private int level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotItem {
        private String date;
        private String time;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItem {
        private Long id;
        private String reviewer;
        private Integer rating;
        private String date;
        private String content;
        private Integer helpful;

        public static ReviewItem from(MentorReview review) {
            return ReviewItem.builder()
                    .id(review.getId())
                    .reviewer(review.getReviewer().getNickname())
                    .rating(review.getRating())
                    .date(review.getCreatedAt().toLocalDate().toString())
                    .content(review.getContent())
                    .helpful(review.getHelpfulCount())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseStats {
        private String averageResponseTime;
        private int responseRate;
        private int repeatRate;
        private Integer totalMentoring;
    }

    /**
     * 멘토 신청 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorApplicationRequest {
        private String name;
        private String title;
        private String company;
        private String experience;
        private List<String> expertise;
        private String bio;
        private String menteeCapacity;
        private List<String> availability;
        private String preferredFormat;
        private List<String> certificates;
    }

    /**
     * 멘토 신청 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyMentorResponse {
        private boolean success;
        private String message;
        private Long mentorId;
    }
}
