package com.certifolio.server.domain.analytics.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "analytics")
public class Analytic extends BaseTimeEntity {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int overallScore;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String categoryScoresJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String strengthsJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String improvementsJson;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Builder
    public Analytic(User user, int overallScore, Map<String, Integer> categoryScores,
                           List<String> strengths, List<String> improvements, String summary) {
        this.user = user;
        this.overallScore = overallScore;
        this.categoryScoresJson = toJson(categoryScores);
        this.strengthsJson = toJson(strengths);
        this.improvementsJson = toJson(improvements);
        this.summary = summary;
    }

    public Map<String, Integer> getCategoryScores() {
        return fromJson(categoryScoresJson, new TypeReference<>() {});
    }

    public List<String> getStrengths() {
        return fromJson(strengthsJson, new TypeReference<>() {});
    }

    public List<String> getImprovements() {
        return fromJson(improvementsJson, new TypeReference<>() {});
    }

    private static String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(GeneralErrorCode.JSON_PARSE_ERROR);
        }
    }

    private static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new BusinessException(GeneralErrorCode.JSON_PARSE_ERROR);
        }
    }

}
