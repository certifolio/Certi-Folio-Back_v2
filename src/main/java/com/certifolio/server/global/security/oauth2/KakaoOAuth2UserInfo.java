package com.certifolio.server.global.security.oauth2;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> profile = getProfile();
        if (profile == null) return null;
        return (String) profile.get("nickname");
    }

    @Override
    public String getPicture() {
        Map<String, Object> profile = getProfile();
        if (profile == null) return null;
        return (String) profile.get("profile_image_url");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getProfile() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) return null;
        return (Map<String, Object>) kakaoAccount.get("profile");
    }
}


