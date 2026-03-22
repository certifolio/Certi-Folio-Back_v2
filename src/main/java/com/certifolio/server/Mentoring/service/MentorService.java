package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.*;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Mentoring.dto.MentorDTO;
import com.certifolio.server.Mentoring.repository.*;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.User.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MentorService {

    private final MentorRepository mentorRepository;
    private final MentorSkillRepository mentorSkillRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final UserRepository userRepository;
    private final UserService userService; // UserService를 통해 User 관련 데이터 조회

    /**
     * 멘토 검색/목록 조회
     */
    public MentorDTO.MentorsResponse searchMentors(List<String> skills) {
        List<Mentor> mentors;

        if (skills != null && !skills.isEmpty()) {
            mentors = mentorRepository.findBySkillsContaining(skills);
        } else {
            mentors = mentorRepository.findByStatus(MentorStatus.APPROVED);
        }

        List<MentorDTO.MentorListItem> mentorItems = mentors.stream()
                .map(MentorDTO.MentorListItem::from)
                .collect(Collectors.toList());

        return MentorDTO.MentorsResponse.builder()
                .mentors(mentorItems)
                .total(mentorItems.size())
                .build();
    }

    /**
     * 멘토 프로필 상세 조회
     */
    public MentorDTO.MentorProfileResponse getMentorProfile(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));

        // UserService를 통해 User의 학력/경력 정보 조회 (OOP 원칙 준수)
        Long userId = mentor.getUser().getId();
        List<EducationDTO> educationList = userService.getEducationsByUserId(userId);
        List<CareerDTO> careerList = userService.getCareersByUserId(userId);

        return MentorDTO.MentorProfileResponse.from(mentor, educationList, careerList);
    }

    /**
     * 멘토 신청
     */
    @Transactional
    public MentorDTO.ApplyMentorResponse applyMentor(Long userId, MentorDTO.MentorApplicationRequest request) {
        // 이미 멘토인지 확인
        if (mentorRepository.existsByUserId(userId)) {
            return MentorDTO.ApplyMentorResponse.builder()
                    .success(false)
                    .message("이미 멘토 신청을 하셨습니다.")
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 멘토 생성
        Mentor mentor = Mentor.builder()
                .user(user)
                .title(request.getTitle())
                .company(request.getCompany())
                .experience(request.getExperience())
                .bio(request.getBio())
                .preferredFormat(request.getPreferredFormat())
                .status(MentorStatus.PENDING)
                .skills(new ArrayList<>())
                .availabilities(new ArrayList<>())
                .build();

        mentorRepository.save(mentor);

        // 스킬 추가
        if (request.getExpertise() != null) {
            for (String skillName : request.getExpertise()) {
                MentorSkill skill = MentorSkill.builder()
                        .mentor(mentor)
                        .skillName(skillName)
                        .level(3) // 기본값
                        .build();
                mentor.addSkill(skill);
            }
        }

        // 가용 시간 추가
        if (request.getAvailability() != null) {
            for (String timeSlot : request.getAvailability()) {
                MentorAvailability availability = MentorAvailability.builder()
                        .mentor(mentor)
                        .timeSlot(timeSlot)
                        .build();
                mentor.addAvailability(availability);
            }
        }

        mentorRepository.save(mentor);

        log.info("새 멘토 신청: userId={}, mentorId={}", userId, mentor.getId());

        return MentorDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토 신청이 완료되었습니다. 심사 후 연락드리겠습니다.")
                .mentorId(mentor.getId())
                .build();
    }

    /**
     * 내 멘토 프로필 조회
     */
    public MentorDTO.MentorProfileResponse getMyMentorProfile(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("멘토 프로필이 없습니다."));

        return getMentorProfile(mentor.getId());
    }

    /**
     * [어드민] 전체 멘토 신청 목록 조회
     */
    public MentorDTO.AdminMentorsResponse getAdminApplications() {
        List<Mentor> mentors = mentorRepository.findAllByOrderByCreatedAtDesc();
        List<MentorDTO.AdminMentorItem> items = mentors.stream()
                .map(MentorDTO.AdminMentorItem::from)
                .collect(Collectors.toList());
        return MentorDTO.AdminMentorsResponse.builder()
                .mentors(items)
                .total(items.size())
                .build();
    }

    /**
     * [어드민] 멘토 승인
     */
    @Transactional
    public MentorDTO.ApplyMentorResponse approveMentor(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));
        mentor.setStatus(MentorStatus.APPROVED);
        mentorRepository.save(mentor);
        log.info("멘토 승인: mentorId={}", mentorId);
        return MentorDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토가 승인되었습니다.")
                .mentorId(mentorId)
                .build();
    }

    /**
     * [어드민] 멘토 거절
     */
    @Transactional
    public MentorDTO.ApplyMentorResponse rejectMentor(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));
        mentor.setStatus(MentorStatus.REJECTED);
        mentorRepository.save(mentor);
        log.info("멘토 거절: mentorId={}", mentorId);
        return MentorDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토가 거절되었습니다.")
                .mentorId(mentorId)
                .build();
    }

    /**
     * 멘토 프로필 업데이트
     */
    @Transactional
    public MentorDTO.ApplyMentorResponse updateMentorProfile(Long userId, MentorDTO.MentorApplicationRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("멘토 프로필이 없습니다."));

        mentor.setTitle(request.getTitle());
        mentor.setCompany(request.getCompany());
        mentor.setExperience(request.getExperience());
        mentor.setBio(request.getBio());
        mentor.setPreferredFormat(request.getPreferredFormat());

        // 스킬 업데이트 (기존 삭제 후 재생성)
        mentor.getSkills().clear();
        if (request.getExpertise() != null) {
            for (String skillName : request.getExpertise()) {
                MentorSkill skill = MentorSkill.builder()
                        .mentor(mentor)
                        .skillName(skillName)
                        .level(3)
                        .build();
                mentor.addSkill(skill);
            }
        }

        // 가용 시간 업데이트
        mentor.getAvailabilities().clear();
        if (request.getAvailability() != null) {
            for (String timeSlot : request.getAvailability()) {
                MentorAvailability availability = MentorAvailability.builder()
                        .mentor(mentor)
                        .timeSlot(timeSlot)
                        .build();
                mentor.addAvailability(availability);
            }
        }

        mentorRepository.save(mentor);

        return MentorDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토 프로필이 업데이트되었습니다.")
                .mentorId(mentor.getId())
                .build();
    }
}
