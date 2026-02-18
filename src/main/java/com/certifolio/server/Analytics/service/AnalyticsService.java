package com.certifolio.server.Analytics.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Analytics 도메인 서비스
 * Controller -> Service -> Repository 계층 구조를 따름
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    public Map<String, Object> getSkillAnalysis() {
        return Map.of(
                "skills", List.of(
                        Map.of("subject", "자격증", "myScore", 0, "targetScore", 80, "fullMark",
                                100),
                        Map.of("subject", "어학", "myScore", 0, "targetScore", 85, "fullMark",
                                100),
                        Map.of("subject", "경력", "myScore", 0, "targetScore", 75, "fullMark",
                                100),
                        Map.of("subject", "학점", "myScore", 0, "targetScore", 80, "fullMark",
                                100),
                        Map.of("subject", "프로젝트", "myScore", 0, "targetScore", 85, "fullMark",
                                100),
                        Map.of("subject", "대외활동", "myScore", 0, "targetScore", 70, "fullMark",
                                100)),
                "strengths", List.of(),
                "weaknesses", List.of(),
                "recommendations", List.of(),
                "overallScore", 0);
    }
}
