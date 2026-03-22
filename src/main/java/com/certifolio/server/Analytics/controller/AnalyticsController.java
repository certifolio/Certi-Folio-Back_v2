package com.certifolio.server.Analytics.controller;

import com.certifolio.server.Analytics.dto.AnalyticsResultDTO;
import com.certifolio.server.Analytics.service.AnalyticsService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/portfolio")
    public ResponseEntity<?> analyzePortfolio(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        AnalyticsResultDTO result = analyticsService.analyzePortfolio(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    }
}
