package com.certifolio.server.global.security.oauth2;

import com.certifolio.server.domain.user.entity.Role;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo userInfo = resolveUserInfo(registrationId, oAuth2User.getAttributes());
        User user = findOrCreateUser(userInfo);

        // OAuth2SuccessHandler에서 userId를 꺼낼 수 있도록 attributes에 추가
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                attributes,
                nameAttributeKey
        );
    }

    private OAuth2UserInfo resolveUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao"  -> new KakaoOAuth2UserInfo(attributes);
            case "naver"  -> new NaverOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "지원하지 않는 OAuth 제공자입니다: " + registrationId
            );
        };
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .orElseGet(() -> {
                    log.info("신규 유저 생성: provider={}, providerId={}", userInfo.getProvider(), userInfo.getProviderId());
                    return userRepository.save(User.builder()
                            .name(userInfo.getName())
                            .email(userInfo.getEmail())
                            .picture(userInfo.getPicture())
                            .provider(userInfo.getProvider())
                            .providerId(userInfo.getProviderId())
                            .role(Role.USER)
                            .build());
                });
    }
}
