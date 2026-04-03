package com.certifolio.server.global.security.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();

    String getProvider();

    String getEmail();

    String getName();

    String getPicture();
}
