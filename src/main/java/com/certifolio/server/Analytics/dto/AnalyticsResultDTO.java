package com.certifolio.server.Analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResultDTO {
    private int overallScore;
    private Map<String, Integer> categoryScores;
    private List<String> strengths;
    private List<String> improvements;
    private String summary;
}
