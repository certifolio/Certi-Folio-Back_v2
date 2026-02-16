package com.certifolio.server.config;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentorStatus;
import com.certifolio.server.Mentoring.domain.MentoringSession;
import com.certifolio.server.Mentoring.domain.SessionStatus;
import com.certifolio.server.Mentoring.repository.MentorRepository;
import com.certifolio.server.Mentoring.repository.MentoringSessionRepository;
import com.certifolio.server.User.domain.Role;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * 채팅 테스트용 초기 데이터 생성
 * 서버 시작 시 테스트 User, Mentor, MentoringSession을 자동 생성합니다.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChatDataInitializer {

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;
    private final MentoringSessionRepository sessionRepository;

    @Bean
    public CommandLineRunner initChatTestData() {
        return args -> {
            // 이미 테스트 데이터가 있으면 스킵
            if (userRepository.findByProviderAndProviderId("test", "mentor1").isPresent()) {
                log.info("채팅 테스트 데이터가 이미 존재합니다. (sessionId는 DB에서 확인)");
                // 기존 세션 ID 출력
                userRepository.findByProviderAndProviderId("test", "mentor1").ifPresent(mentorUser -> {
                    mentorRepository.findByUserId(mentorUser.getId()).ifPresent(mentor -> {
                        var sessions = sessionRepository.findByMentorId(mentor.getId());
                        if (!sessions.isEmpty()) {
                            log.info("=== 기존 채팅 테스트 세션 ID: {} ===", sessions.get(0).getId());
                        }
                    });
                });
                return;
            }

            log.info("=== 채팅 테스트 데이터 생성 시작 ===");

            // 1. 멘토 User 생성
            User mentorUser = User.builder()
                    .name("김멘토")
                    .nickname("mentor_kim")
                    .email("mentor@test.com")
                    .role(Role.USER)
                    .provider("test")
                    .providerId("mentor1")
                    .build();
            mentorUser = userRepository.save(mentorUser);
            log.info("멘토 User 생성: id={}, subject=test:mentor1", mentorUser.getId());

            // 2. 멘티 User 생성
            User menteeUser = User.builder()
                    .name("이멘티")
                    .nickname("mentee_lee")
                    .email("mentee@test.com")
                    .role(Role.USER)
                    .provider("test")
                    .providerId("mentee1")
                    .build();
            menteeUser = userRepository.save(menteeUser);
            log.info("멘티 User 생성: id={}, subject=test:mentee1", menteeUser.getId());

            // 3. Mentor 엔티티 생성
            Mentor mentor = Mentor.builder()
                    .user(mentorUser)
                    .title("Senior Developer")
                    .company("테스트 회사")
                    .experience("5년 이상")
                    .bio("채팅 테스트를 위한 멘토입니다.")
                    .location("서울")
                    .price("무료")
                    .verified(true)
                    .status(MentorStatus.APPROVED)
                    .rating(4.5)
                    .reviewCount(10)
                    .build();
            mentor = mentorRepository.save(mentor);
            log.info("Mentor 생성: id={}", mentor.getId());

            // 4. MentoringSession 생성
            MentoringSession session = MentoringSession.builder()
                    .mentor(mentor)
                    .mentee(menteeUser)
                    .topic("채팅 테스트 세션")
                    .status(SessionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .totalSessions(5)
                    .completedSessions(0)
                    .build();
            session = sessionRepository.save(session);

            log.info("=== 채팅 테스트 데이터 생성 완료 ===");
            log.info("  멘토 subject: test:mentor1");
            log.info("  멘티 subject: test:mentee1");
            log.info("  세션 ID: {}", session.getId());
            log.info("  테스트 페이지: http://localhost:8080/chat-test.html");
        };
    }
}
