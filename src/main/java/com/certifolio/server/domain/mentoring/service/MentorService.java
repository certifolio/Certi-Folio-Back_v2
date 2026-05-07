package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.*;
import com.certifolio.server.domain.notification.entity.NotificationType;
import com.certifolio.server.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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
     * 내 멘토 프로필 조회 (미등록 시 null 반환)
     */
    public MentorResponseDTO.MentorProfileResponse getMyMentorProfile(Long userId) {
        return mentorRepository.findByUserId(userId)
                .map(this::buildMentorProfileResponse)
                .orElse(null);
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

    // ===== 관리자 기능 =====

    /**
     * 멘토 신청 목록 조회 (관리자용)
     * status 파라미터가 null이면 전체 조회
     */
    public MentorResponseDTO.AdminMentorListResponse getAdminMentorList(MentorStatus status) {
        List<Mentor> mentors = (status != null)
                ? mentorRepository.findByStatus(status)
                : mentorRepository.findAllOrderByCreatedAtDesc();

        List<MentorResponseDTO.AdminMentorListItem> items = mentors.stream()
                .map(MentorResponseDTO.AdminMentorListItem::from)
                .collect(Collectors.toList());

        return MentorResponseDTO.AdminMentorListResponse.builder()
                .mentors(items)
                .total(items.size())
                .build();
    }

    /**
     * 멘토 승인 (관리자용)
     */
    @Transactional
    public MentorResponseDTO.AdminMentorActionResponse approveMentor(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

        mentor.approve();

        log.info("멘토 승인: mentorId={}, userId={}", mentorId, mentor.getUser().getId());

        notificationService.createNotification(
                mentor.getUser(),
                NotificationType.MENTORING,
                "멘토 신청 승인",
                "멘토 신청이 승인되었습니다. 이제 멘토로 활동할 수 있습니다.",
                "/mentors/me"
        );

        return MentorResponseDTO.AdminMentorActionResponse.builder()
                .success(true)
                .message("멘토 신청이 승인되었습니다.")
                .mentorId(mentorId)
                .status(MentorStatus.APPROVED.name())
                .build();
    }

    /**
     * 멘토 거절 (관리자용)
     */
    @Transactional
    public MentorResponseDTO.AdminMentorActionResponse rejectMentor(Long mentorId, String reason) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

        mentor.reject(reason);

        log.info("멘토 거절: mentorId={}, userId={}, reason={}", mentorId, mentor.getUser().getId(), reason);

        String notificationMessage = (reason != null && !reason.isBlank())
                ? "멘토 신청이 거절되었습니다. 사유: " + reason
                : "멘토 신청이 거절되었습니다.";

        notificationService.createNotification(
                mentor.getUser(),
                NotificationType.MENTORING,
                "멘토 신청 거절",
                notificationMessage,
                "/mentors/apply"
        );

        return MentorResponseDTO.AdminMentorActionResponse.builder()
                .success(true)
                .message("멘토 신청이 거절되었습니다.")
                .mentorId(mentorId)
                .status(MentorStatus.REJECTED.name())
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
