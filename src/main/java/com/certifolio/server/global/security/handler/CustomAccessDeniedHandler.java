package com.certifolio.server.global.security.handler;

import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        GeneralErrorCode errorCode = GeneralErrorCode.FORBIDDEN;

        log.warn("Forbidden request: {} {}", request.getMethod(), request.getRequestURI());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(buildJson(errorCode.getCode(), errorCode.getMessage()));
    }

    private String buildJson(String code, String message) {
        return String.format(
                "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\",\"result\":null,\"error\":null,\"timestamp\":null}",
                code, message
        );
    }
}
