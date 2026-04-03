package com.certifolio.server.global.security.handler;

import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        GeneralErrorCode errorCode = GeneralErrorCode.UNAUTHORIZED;

        log.warn("Unauthorized request: {} {}", request.getMethod(), request.getRequestURI());

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
