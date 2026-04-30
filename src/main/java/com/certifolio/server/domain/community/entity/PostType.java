package com.certifolio.server.domain.community.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostType {

    GENERAL,
    COMPANY,
    STUDY,
    ETC;
}
