package com.certifolio.server.auth.util;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 인증 관련 유틸리티
 * Principal에서 User 엔티티를 추출하는 공통 로직
 */
public class AuthUtils {

    /**
     * Spring Security principal에서 User를 조회한다.
     * principal이 UserDetails면 username(subject)을 추출하고,
     * String이면 그대로 subject로 사용한다.
     * subject가 "provider:providerId" 형식이면 해당 조건으로 조회,
     * 그 외에는 email로 조회한다.
     */
    public static User resolveUser(Object principal, UserRepository userRepository) {
        String subject = null;
        if (principal instanceof UserDetails) {
            subject = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            subject = (String) principal;
        }

        if (subject == null) {
            return null;
        }

        if (subject.contains(":")) {
            String[] parts = subject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
        } else {
            return userRepository.findByEmail(subject).orElse(null);
        }
    }
}
