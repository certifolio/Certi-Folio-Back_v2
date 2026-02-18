package com.certifolio.server.auth.userinfo;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccount;
    private Map<String, Object> profile;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (this.kakaoAccount != null) {
            this.profile = (Map<String, Object>) kakaoAccount.get("profile");
        }
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
        // Kakao might not provide email
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return (String) kakaoAccount.get("email");
        }
        return null;
    }

    @Override
    public String getName() {
        if (profile != null && profile.containsKey("nickname")) {
            return (String) profile.get("nickname"); // Kakao 'nickname' is actually 'name' in profile
        }
        return null;
    }

    @Override
    public String getPicture() {
        if (profile != null && profile.containsKey("profile_image_url")) {
            return (String) profile.get("profile_image_url");
        }
        return null;
    }
}

