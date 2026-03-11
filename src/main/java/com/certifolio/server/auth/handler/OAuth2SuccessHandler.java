package com.certifolio.server.auth.handler;

import com.certifolio.server.User.domain.Role;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.auth.jwt.JwtTokenProvider;
import com.certifolio.server.auth.userinfo.GoogleOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.KakaoOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.NaverOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.OAuth2UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://3.35.37.53/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String provider = authToken.getAuthorizedClientRegistrationId();

        OAuth2UserInfo userInfo = getOAuth2UserInfo(provider, oAuth2User.getAttributes());
        
        if (userInfo == null) {
            throw new RuntimeException("Unsupported provider: " + provider);
        }

        // Find or create user
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(entity -> entity.update(userInfo.getName(), userInfo.getPicture()))
                .orElse(User.builder()
                        .name(userInfo.getName())
                        .email(userInfo.getEmail())
                        .picture(userInfo.getPicture())
                        .role(Role.USER) // Default role
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .build());
        
        userRepository.save(user);

        // Generate Token
        String token = jwtTokenProvider.createToken(provider, userInfo.getProviderId(), user.getRoleKey());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("isInfoInputted", user.isInfoInputted())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoOAuth2UserInfo(attributes);
        } else {
            return null;
        }
    }
}
