package com.certifolio.server.domain.analytics.service;

import com.certifolio.server.domain.analytics.dto.response.AnalyticResponseDTO;
import com.certifolio.server.domain.analytics.entity.Analytic;
import com.certifolio.server.domain.analytics.repository.AnalyticRepository;
import com.certifolio.server.domain.form.activity.dto.response.ActivityResponseDTO;
import com.certifolio.server.domain.form.activity.service.ActivityService;
import com.certifolio.server.domain.form.algorithm.dto.response.AlgorithmResponseDTO;
import com.certifolio.server.domain.form.algorithm.repository.AlgorithmRepository;
import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.career.service.CareerService;
import com.certifolio.server.domain.form.certificate.dto.response.CertificateResponseDTO;
import com.certifolio.server.domain.form.certificate.service.CertificateService;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.form.education.service.EducationService;
import com.certifolio.server.domain.form.project.dto.response.ProjectResponseDTO;
import com.certifolio.server.domain.form.project.service.ProjectService;
import com.certifolio.server.domain.user.entity.CareerPreference;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.repository.CareerPreferenceRepository;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticService {

    private final UserService userService;
    private final AnalyticRepository analyticRepository;
    private final CareerPreferenceRepository careerPreferenceRepository;
    private final ActivityService activityService;
    private final AlgorithmRepository algorithmRepository;
    private final CareerService careerService;
    private final CertificateService certificateService;
    private final EducationService educationService;
    private final ProjectService projectService;
    private final GeminiService geminiService;
    private final AnalyticPromptBuilder analyticPromptBuilder;

    // 최신 분석 결과 조회
    public AnalyticResponseDTO getLatestAnalytic(Long userId) {
        Analytic analytic = analyticRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.ANALYTICS_NOT_FOUND));

        return AnalyticResponseDTO.from(analytic);
    }

    // 분석 이력 전체 조회
    public List<AnalyticResponseDTO> getAnalyticHistory(Long userId) {
        return analyticRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AnalyticResponseDTO::from)
                .toList();
    }

    // 포트폴리오 분석 요청
    @Transactional
    public AnalyticResponseDTO analyzePortfolio(Long userId) {
        User user = userService.getUserById(userId);

        List<ActivityResponseDTO> activities = activityService.getActivities(userId);
        List<CareerResponseDTO> careers = careerService.getCareers(userId);
        List<CertificateResponseDTO> certificates = certificateService.getCertificates(userId);
        AlgorithmResponseDTO algorithm = algorithmRepository.findByUserId(userId)
                .map(AlgorithmResponseDTO::from)
                .orElse(null);
        List<EducationResponseDTO> educations = educationService.getEducations(userId);
        List<ProjectResponseDTO> projects = projectService.getProjects(userId);
        CareerPreference preference = careerPreferenceRepository.findByUser(user).orElse(null);

        String prompt = analyticPromptBuilder.build(educations, careers, certificates, projects, activities, algorithm, preference);
        AnalyticResponseDTO result = geminiService.analyze(prompt);

        Analytic analytic = analyticRepository.save(Analytic.builder()
                .user(user)
                .overallScore(result.overallScore())
                .categoryScores(result.categoryScores())
                .strengths(result.strengths())
                .improvements(result.improvements())
                .summary(result.summary())
                .build());

        return AnalyticResponseDTO.from(analytic);
    }

}
