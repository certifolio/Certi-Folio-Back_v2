package com.certifolio.server.Analytics.controller;

import com.certifolio.server.Analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Analytics 컨트롤러
 * Service 레이어를 통해 비즈니스 로직에 접근 (OOP 원칙 준수)
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/skill-analysis")
    public ResponseEntity<?> getSkillAnalysis(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(analyticsService.getSkillAnalysis());
    }
}
