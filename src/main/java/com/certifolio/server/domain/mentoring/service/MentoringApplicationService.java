package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.*;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.MentoringApplicationDTO;
import com.certifolio.server.Mentoring.repository.MentorRepository;
import com.certifolio.server.Mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.Mentoring.repository.MentoringSessionRepository;
import com.certifolio.server.User.repository.UserRepository;
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
        public MentoringApplicationDTO.CreateResponse createApplication(
                        Long userId, MentoringApplicationDTO.CreateRequest request) {

                User mentee = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                Mentor mentor = mentorRepository.findById(request.mentorId())
                                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));

                // 자기 자신에게 신청할 수 없음
                if (mentor.getUser().getId().equals(userId)) {
                        return MentoringApplicationDTO.CreateResponse.builder()
                                        .success(false)
                                        .message("자기 자신에게 멘토링을 신청할 수 없습니다.")
                                        .build();
                }

                // 이미 대기 중인 신청이 있는지 확인
                boolean alreadyApplied = applicationRepository.existsByMenteeIdAndMentorIdAndStatus(
                                userId, mentor.getId(), ApplicationStatus.PENDING);
                if (alreadyApplied) {
                        return MentoringApplicationDTO.CreateResponse.builder()
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

                return MentoringApplicationDTO.CreateResponse.builder()
                                .success(true)
                                .message("멘토링 신청이 완료되었습니다!")
                                .applicationId(application.getId())
                                .build();
        }

        /**
         * 받은 신청 목록 조회 (멘토용)
         */
        public MentoringApplicationDTO.ApplicationsResponse getReceivedApplications(Long userId) {
                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElse(null);

                if (mentor == null) {
                        return MentoringApplicationDTO.ApplicationsResponse.builder()
                                        .applications(List.of())
                                        .total(0)
                                        .build();
                }

                List<MentoringApplication> applications = applicationRepository.findByMentorId(mentor.getId());

                List<MentoringApplicationDTO.ApplicationItem> items = applications.stream()
                                .map(MentoringApplicationDTO.ApplicationItem::from)
                                .collect(Collectors.toList());

                return MentoringApplicationDTO.ApplicationsResponse.builder()
                                .applications(items)
                                .total(items.size())
                                .build();
        }

        /**
         * 보낸 신청 목록 조회 (멘티용)
         */
        public MentoringApplicationDTO.ApplicationsResponse getSentApplications(Long userId) {
                List<MentoringApplication> applications = applicationRepository.findByMenteeId(userId);

                List<MentoringApplicationDTO.ApplicationItem> items = applications.stream()
                                .map(MentoringApplicationDTO.ApplicationItem::from)
                                .collect(Collectors.toList());

                return MentoringApplicationDTO.ApplicationsResponse.builder()
                                .applications(items)
                                .total(items.size())
                                .build();
        }

        /**
         * 신청 승인
         */
        @Transactional
        public MentoringApplicationDTO.ActionResponse approveApplication(Long userId, Long applicationId) {
                MentoringApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다."));

                // 본인의 멘토 계정인지 확인
                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("멘토 권한이 필요합니다."));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        return MentoringApplicationDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("본인에게 온 신청만 승인할 수 있습니다.")
                                        .build();
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        return MentoringApplicationDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("이미 처리된 신청입니다.")
                                        .build();
                }

                // 신청 승인
                application.setStatus(ApplicationStatus.APPROVED);
                applicationRepository.save(application);

                // 멘토링 세션 생성
                MentoringSession session = MentoringSession.builder()
                                .mentor(mentor)
                                .mentee(application.getMentee())
                                .topic(application.getTopic())
                                .status(SessionStatus.ACTIVE)
                                .startDate(LocalDate.now())
                                .build();

                sessionRepository.save(session);

                log.info("멘토링 신청 승인: applicationId={}, sessionId={}", applicationId, session.getId());

                return MentoringApplicationDTO.ActionResponse.builder()
                                .success(true)
                                .message("신청을 승인했습니다. 멘토링이 시작됩니다!")
                                .sessionId(session.getId())
                                .build();
        }

        /**
         * 신청 거절
         */
        @Transactional
        public MentoringApplicationDTO.ActionResponse rejectApplication(
                        Long userId, Long applicationId, String reason) {

                MentoringApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다."));

                // 본인의 멘토 계정인지 확인
                Mentor mentor = mentorRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("멘토 권한이 필요합니다."));

                if (!application.getMentor().getId().equals(mentor.getId())) {
                        return MentoringApplicationDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("본인에게 온 신청만 거절할 수 있습니다.")
                                        .build();
                }

                if (application.getStatus() != ApplicationStatus.PENDING) {
                        return MentoringApplicationDTO.ActionResponse.builder()
                                        .success(false)
                                        .message("이미 처리된 신청입니다.")
                                        .build();
                }

                // 신청 거절
                application.setStatus(ApplicationStatus.REJECTED);
                application.setRejectReason(reason);
                applicationRepository.save(application);

                log.info("멘토링 신청 거절: applicationId={}", applicationId);

                return MentoringApplicationDTO.ActionResponse.builder()
                                .success(true)
                                .message("신청을 거절했습니다.")
                                .build();
        }
}
