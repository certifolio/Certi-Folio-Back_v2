package com.certifolio.server.domain.form.controller;

import com.certifolio.server.domain.form.activity.service.ActivityService;
import com.certifolio.server.domain.form.algorithm.service.AlgorithmService;
import com.certifolio.server.domain.form.career.service.CareerService;
import com.certifolio.server.domain.form.certificate.service.CertificateService;
import com.certifolio.server.domain.form.education.service.EducationService;
import com.certifolio.server.domain.form.project.service.ProjectService;
import com.certifolio.server.domain.user.dto.response.UserResponseDTO;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/specs")
@RequiredArgsConstructor
public class SpecController {

    private final UserService userService;

    private final ActivityService activityService;
    private final AlgorithmService algorithmService;
    private final CareerService careerService;
    private final CertificateService certificateService;
    private final EducationService educationService;
    private final ProjectService projectService;

    // 스펙 전체 조회
    @GetMapping("/all")
    public ApiResponse<Map<String, Object>> getAllSpecs(@AuthenticationPrincipal Long userId) {
        UserResponseDTO user = UserResponseDTO.from(userService.getUserById(userId));

        Map<String, Object> specs = new HashMap<>();
        specs.put("activity", activityService.getActivities(userId));
        specs.put("algorithm", getAlgorithmOrNull(userId));
        specs.put("career", careerService.getCareers(userId));
        specs.put("certificate", certificateService.getCertificates(userId));
        specs.put("education", educationService.getEducations(userId));
        specs.put("project", projectService.getProjects(userId));

        return ApiResponse.onSuccess("전체 스펙 조회 성공", Map.of("user", user, "specs", specs));
    }

    private Object getAlgorithmOrNull(Long userId) {
        try {
            return algorithmService.getAlgorithm(userId);
        } catch (BusinessException e) {
            if (e.getErrorCode() == GeneralErrorCode.ALGORITHM_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }
}
