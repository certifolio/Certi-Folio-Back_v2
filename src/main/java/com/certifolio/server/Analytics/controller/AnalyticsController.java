package com.certifolio.server.Analytics.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.CareerPreferenceDTO;
import com.certifolio.server.Analytics.service.AnalyticsService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Analytics 컨트롤러
 * Service 레이어를 통해 비즈니스 로직에 접근 (OOP 원칙 준수)
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthenticationHelper authenticationHelper;



    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        User user = analyticsService.getUserById(userId);

        return ResponseEntity.ok(Map.of("preference",
                analyticsService.getPreferences(user).orElse(null)));
    }

    @PostMapping("/preferences")
    public ResponseEntity<?> savePreferences(@AuthenticationPrincipal Object principal,
            @RequestBody CareerPreferenceDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        User user = analyticsService.getUserById(userId);

        analyticsService.savePreferences(user, dto);

        return ResponseEntity.ok(Map.of("success", true, "message", "저장되었습니다."));
    }

    @GetMapping("/skill-analysis")
    public ResponseEntity<?> getSkillAnalysis(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(analyticsService.getSkillAnalysis());
    }

    @PostMapping("/skill-analysis/refresh")
    public ResponseEntity<?> refreshSkillAnalysis(@AuthenticationPrincipal Object principal) {
        return getSkillAnalysis(principal);
    }
}
