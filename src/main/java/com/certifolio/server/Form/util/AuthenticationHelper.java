package com.certifolio.server.Form.util;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final UserRepository userRepository;

    /**
     * Get User ID from authentication principal
     * Supports both email and provider:id formats
     */
    public Long getUserId(Object principal) {
        String subject = principal.toString();
        
        if (subject.contains(":")) {
            String[] parts = subject.split(":");
            return userRepository.findByProviderAndProviderId(parts[0], parts[1])
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            return userRepository.findByEmail(subject)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
    }
}
