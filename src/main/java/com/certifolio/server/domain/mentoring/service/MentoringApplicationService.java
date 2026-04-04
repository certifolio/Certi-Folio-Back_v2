package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.*;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.mentoring.dto.request.MentoringApplicationRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentoringApplicationResponseDTO;
import com.certifolio.server.domain.mentoring.repository.MentorRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringSessionRepository;
import com.certifolio.server.domain.user.repository.UserRepository;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MentoringApplicationService {

        private final MentoringApplicationRepository applicationRepository;
        private final MentorRepository mentorRepository;
        private final MentoringSessionRepository sessionRepository;
        private final UserRepository userRepository;

        /**
         * 멘토링 신청
         */
        @Transactional
        public MentoringApplicationResponseDTO.CreateResponse createApplication(
                        Long userId, MentoringApplicationRequestDTO.CreateRequest request) {

                User mentee = userRepository.findById(userId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));

                Mentor mentor = mentorRepository.findById(request.mentorId())
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

                if (mentor.getUser().getId().equals(userId)) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_SELF_APPLICATION);
                }

                boolean alreadyApplied = applicationRepository.existsByMenteeIdAndMentorIdAndStatus(
                                userId, mentor.getId(), ApplicationStatus.PENDING);
                if (alreadyApplied) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_ALREADY_APPLIED);
                }

                MentoringApplication application = MentoringApplication.builder()
                                .mentee(mentee)
                                .mentor(mentor)
                                .topic(request.topic())
                                .description(request.description())
                                .status(ApplicationStatus.PENDING)
                                .build();

                applicationRepository.save(application);

                log.info("멘토링 신청 생성: menteeId={}, mentorId={}, applicationId={}",
                                userId, mentor.getId(), application.getId());

                return MentoringApplicationResponseDTO.CreateResponse.builder()
                                .success(true)
                                .message("멘토링 신청이 완료되었습니다!")
                                .applicationId(application.getId())
                                .build();
        }

        /**
         * 받은 신청 목록 조회 (멘토용)
         */
        public MentoringApplicationResponseDTO.ApplicationsResponse getReceivedApplications(Long userId) {
                Mentor mentor = mentorRepository.findByUserId(userId).orElse(null);

                if (mentor == null) {
                        return MentoringApplicationResponseDTO.ApplicationsResponse.builder()
                                        .applications(List.of())
                                        .total(0)
                                        .build();
                }

                List<MentoringApplication> applications = applicationRepository.findByMentorId(mentor.getId());

                List<MentoringApplicationResponseDTO.ApplicationItem> items = applications.stream()
                                .map(MentoringApplicationResponseDTO.ApplicationItem::from)
                                .collect(Collectors.toList());

                return MentoringApplicationResponseDTO.ApplicationsResponse.builder()
                                .applications(items)
                                .total(items.size())
                                .build();
        }

        /**
         * 보낸 신청 목록 조회 (멘티용)
         */
        public MentoringApplicationResponseDTO.ApplicationsResponse getSentApplications(Long userId) {
                List<MentoringApplication> applications = applicationRepository.findByMenteeId(userId);

                List<MentoringApplicationResponseDTO.ApplicationItem> items = applications.stream()
                                .map(MentoringApplicationResponseDTO.ApplicationItem::from)
                                .collect(Collectors.toList());

                return MentoringApplicationResponseDTO.ApplicationsResponse.builder()
                                .applications(items)
                                .total(items.size())
                                .build();
        }

        /**
         * 신청 승인
         */
        @Transactional
        public MentoringApplicationResponseDTO.ActionResponse approveApplication(Long userId, Long applicationId) {
                MentoringApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTORING_APPLICATION_NOT_FOUND));

                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_APPLICATION_UNAUTHORIZED);
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_ALREADY_PROCESSED);
                }

                application.setStatus(ApplicationStatus.APPROVED);
                applicationRepository.save(application);

                MentoringSession session = MentoringSession.builder()
                                .mentor(mentor)
                                .mentee(application.getMentee())
                                .topic(application.getTopic())
                                .status(SessionStatus.ACTIVE)
                                .startDate(LocalDate.now())
                                .build();

                sessionRepository.save(session);

                log.info("멘토링 신청 승인: applicationId={}, sessionId={}", applicationId, session.getId());

                return MentoringApplicationResponseDTO.ActionResponse.builder()
                                .success(true)
                                .message("신청을 승인했습니다. 멘토링이 시작됩니다!")
                                .sessionId(session.getId())
                                .build();
        }

        /**
         * 신청 거절
         */
        @Transactional
        public MentoringApplicationResponseDTO.ActionResponse rejectApplication(
                        Long userId, Long applicationId, String reason) {

                MentoringApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTORING_APPLICATION_NOT_FOUND));

                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_APPLICATION_UNAUTHORIZED);
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_ALREADY_PROCESSED);
                }

                application.setStatus(ApplicationStatus.REJECTED);
                application.setRejectReason(reason);
                applicationRepository.save(application);

                log.info("멘토링 신청 거절: applicationId={}", applicationId);

                return MentoringApplicationResponseDTO.ActionResponse.builder()
                                .success(true)
                                .message("신청을 거절했습니다.")
                                .build();
        }
}
