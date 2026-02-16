package com.certifolio.server.auth.util;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 인증 관련 공통 유틸리티
 * 컨트롤러에서 Principal 객체로부터 User를 조회하는 공통 로직 제공
 */
public final class AuthUtils {

    private AuthUtils() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * Principal 객체에서 User 엔티티를 조회합니다.
     * Token subject는 항상 "provider:providerId" 형식입니다.
     *
     * @param principal      Spring Security의 AuthenticationPrincipal
     * @param userRepository User 조회용 리포지토리
     * @return User 엔티티 또는 null (인증 정보가 없는 경우)
     */
    public static User resolveUser(Object principal, UserRepository userRepository) {
        String subject = extractSubject(principal);
        if (subject == null) {
            return null;
        }

        // Token subject는 항상 "provider:providerId" 형식
        if (subject.contains(":")) {
            String[] parts = subject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
        }

        return null;
    }

    /**
     * Principal 객체에서 User 엔티티를 조회하고, 없으면 예외를 던집니다.
     *
     * @param principal      Spring Security의 AuthenticationPrincipal
     * @param userRepository User 조회용 리포지토리
     * @return User 엔티티
     * @throws RuntimeException 인증 정보가 없거나 사용자를 찾을 수 없는 경우
     */
    public static User resolveUserOrThrow(Object principal, UserRepository userRepository) {
        User user = resolveUser(principal, userRepository);
        if (user == null) {
            throw new RuntimeException("인증이 필요합니다.");
        }
        return user;
    }

    private static String extractSubject(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }
}
