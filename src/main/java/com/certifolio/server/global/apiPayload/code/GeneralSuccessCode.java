package com.certifolio.server.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK, "COMMON_200", "성공적으로 처리됐습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}