package com.certifolio.server.Analytics.service;

import com.certifolio.server.Analytics.domain.CareerPreference;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.CareerPreferenceDTO;
import com.certifolio.server.Analytics.repository.CareerPreferenceRepository;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analytics 도메인 서비스
 * Controller -> Service -> Repository 계층 구조를 따름
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CareerPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 ID로 User 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * Provider와 ProviderId로 사용자 ID 조회
     */
    public Long getUserIdByProvider(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with provider: " + provider + ", id: " + providerId));
    }

    /**
     * 이메일로 사용자 ID 조회
     */
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * 커리어 선호도 조회
     */
    public Optional<CareerPreferenceDTO> getPreferences(User user) {
        return preferenceRepository.findByUser(user)
                .map(p -> CareerPreferenceDTO.builder()
                        .jobRole(p.getJobRole())
                        .companyType(p.getCompanyType())
                        .targetCompany(p.getTargetCompany())
                        .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString()
                                : null)
                        .build());
    }

    /**
     * 커리어 선호도 저장
     */
    @Transactional
    public void savePreferences(User user, CareerPreferenceDTO dto) {
        CareerPreference pref = preferenceRepository.findByUser(user)
                .orElse(CareerPreference.builder().user(user).build());

        pref.setJobRole(dto.getJobRole());
        pref.setCompanyType(dto.getCompanyType());
        pref.setTargetCompany(dto.getTargetCompany());

        preferenceRepository.save(pref);
    }

    /**
     * 스킬 분석 조회 (현재는 목업 데이터)
     */
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
