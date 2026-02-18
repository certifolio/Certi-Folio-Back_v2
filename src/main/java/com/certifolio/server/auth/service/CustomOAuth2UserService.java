package com.certifolio.server.auth.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.domain.Role;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.auth.userinfo.GoogleOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.KakaoOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.NaverOAuth2UserInfo;
import com.certifolio.server.auth.userinfo.OAuth2UserInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if (registrationId.equals("google")) {
            oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("naver")) {
            oAuth2UserInfo = new NaverOAuth2UserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
        }

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        User user = saveOrUpdate(oAuth2UserInfo);

        // You can return a custom OAuth2User implementation if needed,
        // but for now, returning the default one is often sufficient
        // if we just need attributes in SuccessHandler.
        // However, usually we want to attach our User entity or Role.
        // Let's stick to returning default OAuth2User with our attributes map if
        // needed.
        // Or simpler, we just use the attributes in SuccessHandler to find the user
        // again.

        return oAuth2User; // We will look up user by email/providerId in SuccessHandler if needed, or put
                           // info in attributes.
    }

    private User saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                oAuth2UserInfo.getProvider(),
                oAuth2UserInfo.getProviderId());

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.update(oAuth2UserInfo.getName(), oAuth2UserInfo.getPicture());
                                                         // update it blindly
        } else {
            user = User.builder()
                    .name(oAuth2UserInfo.getName())
                    .email(oAuth2UserInfo.getEmail())
                    .picture(oAuth2UserInfo.getPicture())
                    .role(Role.USER)
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getProviderId())
                    .build();
        }

        return userRepository.save(user);
    }
}
