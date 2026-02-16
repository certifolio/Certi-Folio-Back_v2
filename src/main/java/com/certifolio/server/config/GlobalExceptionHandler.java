package com.certifolio.server.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 글로벌 예외 처리기
 * 모든 컨트롤러에서 발생하는 예외를 통일된 JSON 형식으로 응답
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private boolean success;
        private String message;
    }

    /**
     * IllegalArgumentException 처리 (잘못된 요청 파라미터 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build());
    }

    /**
     * RuntimeException 처리 (일반적인 비즈니스 로직 오류)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("서버 오류: {}", e.getMessage(), e);

        // 인증 관련 오류
        if (e.getMessage() != null && e.getMessage().contains("인증")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }

        // 리소스를 찾을 수 없는 오류
        if (e.getMessage() != null &&
                (e.getMessage().contains("찾을 수 없습니다") || e.getMessage().contains("not found"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }

        // 권한 없음
        if (e.getMessage() != null && e.getMessage().contains("Unauthorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ErrorResponse.builder()
                            .success(false)
                            .message("접근 권한이 없습니다.")
                            .build());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .success(false)
                        .message("서버 내부 오류가 발생했습니다.")
                        .build());
    }

    /**
     * 기타 예상치 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("예상치 못한 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .success(false)
                        .message("서버 내부 오류가 발생했습니다.")
                        .build());
    }
}
