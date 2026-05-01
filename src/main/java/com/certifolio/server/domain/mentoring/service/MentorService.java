package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.*;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.career.service.CareerService;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.form.education.service.EducationService;
import com.certifolio.server.domain.mentoring.dto.request.MentorRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentorResponseDTO;
import com.certifolio.server.domain.mentoring.repository.*;
import com.certifolio.server.domain.user.repository.UserRepository;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
    private final CareerService careerService;
    private final EducationService educationService;

    /**
     * 멘토 검색/목록 조회
     */
    public MentorResponseDTO.MentorsResponse searchMentorBySkills(List<String> skills) {
        List<Mentor> mentors;

        if (skills != null && !skills.isEmpty()) {
            mentors = mentorRepository.findBySkillsContaining(skills);
        } else {
            mentors = mentorRepository.findByStatus(MentorStatus.APPROVED);
        }

        List<MentorResponseDTO.MentorListItem> mentorItems = mentors.stream()
                .map(MentorResponseDTO.MentorListItem::from)
                .collect(Collectors.toList());

        return MentorResponseDTO.MentorsResponse.builder()
                .mentors(mentorItems)
                .total(mentorItems.size())
                .build();
    }

    /**
     * 멘토 프로필 상세 조회
     */
    public MentorResponseDTO.MentorProfileResponse getMentorProfile(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

        return buildMentorProfileResponse(mentor);
    }

    /**
     * 멘토 신청
     */
    @Transactional
    public MentorResponseDTO.ApplyMentorResponse applyMentor(Long userId, MentorRequestDTO.MentorApplicationRequest request) {
        if (mentorRepository.existsByUserId(userId)) {
            throw new BusinessException(GeneralErrorCode.MENTOR_ALREADY_APPLIED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));

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

        return MentorResponseDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토 신청이 완료되었습니다. 심사 후 연락드리겠습니다.")
                .mentorId(mentor.getId())
                .build();
    }

    /**
     * 내 멘토 프로필 조회
     */
    public MentorResponseDTO.MentorProfileResponse getMyMentorProfile(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

        return buildMentorProfileResponse(mentor);
    }

    /**
     * 멘토 프로필 업데이트
     */
    @Transactional
    public MentorResponseDTO.ApplyMentorResponse updateMentorProfile(Long userId, MentorRequestDTO.MentorApplicationRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

        mentor.updateProfile(request.title(), request.company(), request.experience(),
                request.bio(), request.preferredFormat());

        saveSkillsAndAvailabilities(mentor, request);
        mentorRepository.save(mentor);

        return MentorResponseDTO.ApplyMentorResponse.builder()
                .success(true)
                .message("멘토 프로필이 업데이트되었습니다.")
                .mentorId(mentor.getId())
                .build();
    }

    private MentorResponseDTO.MentorProfileResponse buildMentorProfileResponse(Mentor mentor) {
        Long userId = mentor.getUser().getId();
        List<EducationResponseDTO> educationList = educationService.getEducations(userId);
        List<CareerResponseDTO> careerList = careerService.getCareers(userId);

        return MentorResponseDTO.MentorProfileResponse.from(mentor, educationList, careerList);
    }

    private void saveSkillsAndAvailabilities(Mentor mentor, MentorRequestDTO.MentorApplicationRequest request) {
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
            for (MentorRequestDTO.AvailabilityRequest slot : request.availability()) {
                validateAvailability(slot);
                MentorAvailability availability = MentorAvailability.builder()
                        .mentor(mentor)
                        .dayOfWeek(slot.dayOfWeek())
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .slotType(slot.slotType())
                        .build();
                mentor.addAvailability(availability);
            }
        }
    }

    private void validateAvailability(MentorRequestDTO.AvailabilityRequest slot) {
        if (slot.startTime() == null || slot.endTime() == null || !slot.endTime().isAfter(slot.startTime())) {
            throw new BusinessException(GeneralErrorCode.INVALID_TIME_SLOT);
        }

        if (slot.startTime().equals(LocalTime.MAX) || slot.endTime().equals(LocalTime.MIN)) {
            throw new BusinessException(GeneralErrorCode.INVALID_TIME_SLOT);
        }
    }
}
