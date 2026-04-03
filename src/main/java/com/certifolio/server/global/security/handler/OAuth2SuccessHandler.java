package com.certifolio.server.global.security.handler;

import com.certifolio.server.global.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.<Long>getAttribute("userId");
        if (userId == null) {
            log.error("OAuth2 로그인 성공했으나 userId가 없습니다.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> ((SimpleGrantedAuthority) a).getAuthority())
                .orElse("USER");

        String token = tokenProvider.createAccessToken(userId, role);

        log.info("OAuth2 로그인 성공: userId={}", userId);

        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?token=" + token);
    }
}
