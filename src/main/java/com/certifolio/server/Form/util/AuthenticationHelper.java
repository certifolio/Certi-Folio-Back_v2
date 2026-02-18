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
        String subject = null;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            subject = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
             // OAuth2User attributes might vary, but usually we use name or email or a specific attribute
             // Depending on how CustomOAuth2UserService sets it. 
             // Ideally we should use the same logic as UserController.
             // For now, let's assume valid principal is either String (email) or UserDetails.
             // If using Oauth, usually the principal is OAuth2User. 
             // But let's stick to what we know: 
             // In JWT filter, we usually set UsernamePasswordAuthenticationToken with a specific principal.
             // If we set it as String (email), then it is String.
             // If we set it as UserDetails, it is UserDetails.
             try {
                subject = ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getName(); 
             } catch (Exception e) {
                 subject = principal.toString();
             }
        } else if (principal instanceof String) {
            subject = (String) principal;
        } else {
            subject = principal.toString();
        }
        
        if (subject == null) {
             throw new IllegalArgumentException("Invalid principal");
        }

        final String finalSubject = subject;
        
        if (finalSubject.contains(":")) {
            String[] parts = finalSubject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1])
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + finalSubject));
        } else {
            return userRepository.findByEmail(finalSubject)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + finalSubject));
        }
    }
}
