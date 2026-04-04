package com.certifolio.server.domain.analytics.service;

import com.certifolio.server.domain.analytics.dto.response.AnalyticResponseDTO;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticResponseDTO analyze(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json"
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(
                    GEMINI_API_URL + apiKey,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty() || candidates.get(0) == null) {
                throw new BusinessException(GeneralErrorCode.ANALYTICS_API_ERROR);
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isEmpty() || parts.get(0) == null) {
                throw new BusinessException(GeneralErrorCode.ANALYTICS_API_ERROR);
            }

            String jsonText = parts.get(0).path("text").asText();
            return objectMapper.readValue(jsonText, AnalyticResponseDTO.class);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(GeneralErrorCode.ANALYTICS_API_ERROR);
        }
    }
}
