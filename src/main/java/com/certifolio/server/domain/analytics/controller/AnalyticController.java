package com.certifolio.server.domain.analytics.controller;

import com.certifolio.server.domain.analytics.dto.response.AnalyticResponseDTO;
import com.certifolio.server.domain.analytics.service.AnalyticService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticController {

    private final AnalyticService analyticService;

    // 최신 분석 결과 조회
    @GetMapping
    public ApiResponse<AnalyticResponseDTO> getLatestAnalytic(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("분석 결과 조회 성공", analyticService.getLatestAnalytic(userId));
    }

    // 분석 이력 전체 조회
    @GetMapping("/history")
    public ApiResponse<List<AnalyticResponseDTO>> getAnalyticHistory(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("분석 이력 조회 성공", analyticService.getAnalyticHistory(userId));
    }

    // 포트폴리오 분석 요청
    @PostMapping
    public ApiResponse<AnalyticResponseDTO> analyzePortfolio(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("포트폴리오 분석 성공", analyticService.analyzePortfolio(userId));
    }
}
