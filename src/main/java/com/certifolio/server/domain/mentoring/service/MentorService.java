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

        return buildMentorProfileResponse(mentor);
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
                .title(request.title())
                .company(request.company())
                .experience(request.experience())
                .bio(request.bio())
                .preferredFormat(request.preferredFormat())
                .status(MentorStatus.PENDING)
                .skills(new ArrayList<>())
                .availabilities(new ArrayList<>())
                .build();

        mentorRepository.save(mentor);

        saveSkillsAndAvailabilities(mentor, request);

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

        return buildMentorProfileResponse(mentor);
    }

    /**
     * 멘토 프로필 업데이트
     */
    @Transactional
    public MentorDTO.ApplyMentorResponse updateMentorProfile(Long userId, MentorDTO.MentorApplicationRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("멘토 프로필이 없습니다."));

        mentor.setTitle(request.title());
        mentor.setCompany(request.company());
        mentor.setExperience(request.experience());
        mentor.setBio(request.bio());
        mentor.setPreferredFormat(request.preferredFormat());

        // 기존 스킬/가용시간 삭제 후 재생성
        mentor.getSkills().clear();
        mentor.getAvailabilities().clear();

        saveSkillsAndAvailabilities(mentor, request);

        mentorRepository.save(mentor);

        return MentorDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토 프로필이 업데이트되었습니다.")
                .mentorId(mentor.getId())
                .build();
    }

    // ===== Private Helper Methods =====

    /**
     * Mentor 엔티티로부터 프로필 응답 DTO 생성 (공통 로직)
     */
    private MentorDTO.MentorProfileResponse buildMentorProfileResponse(Mentor mentor) {
        Long userId = mentor.getUser().getId();
        List<EducationDTO> educationList = userService.getEducationsByUserId(userId);
        List<CareerDTO> careerList = userService.getCareersByUserId(userId);

        return MentorDTO.MentorProfileResponse.from(mentor, educationList, careerList);
    }

    /**
     * 멘토에 스킬 및 가용시간 저장 (공통 로직)
     */
    private void saveSkillsAndAvailabilities(Mentor mentor, MentorDTO.MentorApplicationRequest request) {
        if (request.expertise() != null) {
            for (String skillName : request.expertise()) {
                MentorSkill skill = MentorSkill.builder()
                        .mentor(mentor)
                        .skillName(skillName)
                        .level(3)
                        .build();
                mentor.addSkill(skill);
            }
        }

        if (request.availability() != null) {
            for (String timeSlot : request.availability()) {
                MentorAvailability availability = MentorAvailability.builder()
                        .mentor(mentor)
                        .timeSlot(timeSlot)
                        .build();
                mentor.addAvailability(availability);
            }
        }
    }
}
