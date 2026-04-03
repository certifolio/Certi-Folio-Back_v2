package com.certifolio.server.global.apiPayload.exception;

import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. BusinessException 처리 (직접 만든 커스텀 예외)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {

        log.warn("BusinessException Occurs: {}", e.getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.onFailure(e.getErrorCode(), null));
    }

    // 2. @Valid 등의 유효성 검사 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");
        log.warn("ValidationException Occurs: {}", errorMessage);
        
        return ResponseEntity
                .status(GeneralErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT, errorMessage));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {

        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT, errorMessage));
    }

    // 3. 그 외 서버에서 발생하는 모든 예외 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {

        log.error("Internal Server Error Occurs: request URI: {}", request.getRequestURI(), e);

        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, null));
    }
}
