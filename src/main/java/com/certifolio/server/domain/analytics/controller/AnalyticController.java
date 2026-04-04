package com.certifolio.server.domain.analytics.controller;

import com.certifolio.server.domain.analytics.dto.response.AnalyticResponseDTO;
import com.certifolio.server.domain.analytics.service.AnalyticService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticController {

    private final AnalyticService analyticService;

    @PostMapping
    public ApiResponse<AnalyticResponseDTO> analyzePortfolio(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("포트폴리오 분석 성공", analyticService.analyzePortfolio(userId));
    }
}
