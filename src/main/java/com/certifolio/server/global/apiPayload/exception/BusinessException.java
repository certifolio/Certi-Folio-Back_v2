package com.certifolio.server.global.apiPayload.exception;

import com.certifolio.server.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public BusinessException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
