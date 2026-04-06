package com.certifolio.server.domain.analytics.dto.response;

import com.certifolio.server.domain.analytics.entity.Analytic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyticResponseDTO(
        Long id,
        int overallScore,
        Map<String, Integer> categoryScores,
        List<String> strengths,
        List<String> improvements,
        String summary
) {
    public static AnalyticResponseDTO from(Analytic analytic) {
        return new AnalyticResponseDTO(
                analytic.getId(),
                analytic.getOverallScore(),
                analytic.getCategoryScores(),
                analytic.getStrengths(),
                analytic.getImprovements(),
                analytic.getSummary()
        );
    }
}
