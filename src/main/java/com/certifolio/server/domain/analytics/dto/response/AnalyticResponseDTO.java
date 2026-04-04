package com.certifolio.server.domain.analytics.dto.response;

import java.util.List;
import java.util.Map;

public record AnalyticResponseDTO(
        int overallScore,
        Map<String, Integer> categoryScores,
        List<String> strengths,
        List<String> improvements,
        String summary
) {}
