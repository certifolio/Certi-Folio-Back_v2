package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.*;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.MentoringSessionDTO;
import com.certifolio.server.Mentoring.repository.MentorRepository;
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
public class MentoringSessionService {

        private final MentoringSessionRepository sessionRepository;
        private final MentorRepository mentorRepository;
        private final UserRepository userRepository;

        /**
         * 내 멘토링 세션 목록 조회
         */
        public MentoringSessionDTO.SessionsResponse getMySessions(Long userId) {
                List<MentoringSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);

                List<MentoringSessionDTO.SessionItem> sessionItems = sessions.stream()
                                .map(MentoringSessionDTO.SessionItem::from)
                                .collect(Collectors.toList());

                return MentoringSessionDTO.SessionsResponse.builder()
                                .sessions(sessionItems)
                                .total(sessionItems.size())
                                .build();
        }

        /**
         * 세션 상세 조회
         */
        public MentoringSessionDTO.SessionItem getSession(Long sessionId) {
                MentoringSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));

                return MentoringSessionDTO.SessionItem.from(session);
        }

        /**
         * 새 세션 생성
         */
        @Transactional
        public MentoringSessionDTO.UpdateSessionResponse createSession(
                        Long userId,
                        MentoringSessionDTO.CreateSessionRequest request) {

                User mentee = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                Mentor mentor = mentorRepository.findById(request.mentorId())
                                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));

                MentoringSession session = MentoringSession.builder()
                                .mentor(mentor)
                                .mentee(mentee)
                                .topic(request.topic())
                                .status(SessionStatus.PENDING)
                                .startDate(LocalDate.now())
                                .build();

                sessionRepository.save(session);

                log.info("새 멘토링 세션 생성: menteeId={}, mentorId={}, sessionId={}",
                                userId, request.mentorId(), session.getId());

                return MentoringSessionDTO.UpdateSessionResponse.builder()
                                .success(true)
                                .message("멘토링 세션이 생성되었습니다.")
                                .build();
        }

        /**
         * 세션 상태 업데이트
         */
        @Transactional
        public MentoringSessionDTO.UpdateSessionResponse updateSessionStatus(
                        Long sessionId,
                        MentoringSessionDTO.UpdateSessionStatusRequest request) {

                MentoringSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));

                try {
                        SessionStatus newStatus = SessionStatus.valueOf(request.status().toUpperCase());
                        session.setStatus(newStatus);

                        sessionRepository.save(session);

                        log.info("세션 상태 업데이트: sessionId={}, newStatus={}", sessionId, newStatus);

                        return MentoringSessionDTO.UpdateSessionResponse.builder()
                                        .success(true)
                                        .message("세션 상태가 업데이트되었습니다.")
                                        .build();

                } catch (IllegalArgumentException e) {
                        return MentoringSessionDTO.UpdateSessionResponse.builder()
                                        .success(false)
                                        .message("잘못된 상태값입니다.")
                                        .build();
                }
        }
}
