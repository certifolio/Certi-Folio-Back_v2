package com.certifolio.server.auth.handler;

import com.certifolio.server.auth.jwt.JwtTokenProvider;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // Always use provider:providerId format for consistency
        Object id = null;

        if (registrationId.equals("naver")) {
            // Naver specific - data is wrapped in "response"
            java.util.Map responseMap = (java.util.Map) oAuth2User.getAttributes().get("response");
            if (responseMap != null) {
                id = responseMap.get("id");
            }
        } else if (registrationId.equals("kakao")) {
            // Kakao uses "id" directly
            id = oAuth2User.getAttributes().get("id");
        } else if (registrationId.equals("google")) {
            // Google uses "sub"
            id = oAuth2User.getAttributes().get("sub");
        }

        if (id == null) {
            // Fallback
            id = oAuth2User.getName();
        }

        System.out.println("OAuth2SuccessHandler: Provider=" + registrationId + ", ID=" + id);

        // Always create token with provider:providerId format
        String token = jwtTokenProvider.createToken(registrationId, String.valueOf(id), "ROLE_USER");

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/callback")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
