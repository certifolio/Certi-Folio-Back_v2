package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.*;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.mentoring.dto.request.MentoringApplicationRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentoringApplicationResponseDTO;
import com.certifolio.server.domain.mentoring.repository.MentorRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringSessionRepository;
import com.certifolio.server.domain.user.repository.UserRepository;
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
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                Mentor mentor = mentorRepository.findById(request.mentorId())
                                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));

                if (mentor.getUser().getId().equals(userId)) {
                        return MentoringApplicationResponseDTO.CreateResponse.builder()
                                        .success(false)
                                        .message("자기 자신에게 멘토링을 신청할 수 없습니다.")
                                        .build();
                }

                boolean alreadyApplied = applicationRepository.existsByMenteeIdAndMentorIdAndStatus(
                                userId, mentor.getId(), ApplicationStatus.PENDING);
                if (alreadyApplied) {
                        return MentoringApplicationResponseDTO.CreateResponse.builder()
                                        .success(false)
                                        .message("이미 대기 중인 신청이 있습니다.")
                                        .build();
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
                                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다."));

                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("멘토 권한이 필요합니다."));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        return MentoringApplicationResponseDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("본인에게 온 신청만 승인할 수 있습니다.")
                                        .build();
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        return MentoringApplicationResponseDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("이미 처리된 신청입니다.")
                                        .build();
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
                                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다."));

                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("멘토 권한이 필요합니다."));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        return MentoringApplicationResponseDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("본인에게 온 신청만 거절할 수 있습니다.")
                                        .build();
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        return MentoringApplicationResponseDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("이미 처리된 신청입니다.")
                                        .build();
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
