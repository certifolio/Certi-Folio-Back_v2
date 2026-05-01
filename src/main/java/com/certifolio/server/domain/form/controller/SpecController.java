package com.certifolio.server.domain.form.controller;

import com.certifolio.server.domain.form.activity.service.ActivityService;
import com.certifolio.server.domain.form.career.service.CareerService;
import com.certifolio.server.domain.form.certificate.service.CertificateService;
import com.certifolio.server.domain.form.dto.response.SpecResponseDTO;
import com.certifolio.server.domain.form.education.service.EducationService;
import com.certifolio.server.domain.form.project.service.ProjectService;
import com.certifolio.server.domain.user.dto.response.UserResponseDTO;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/specs")
@RequiredArgsConstructor
public class SpecController {

    private final UserService userService;
    private final ActivityService activityService;
    private final CareerService careerService;
    private final CertificateService certificateService;
    private final EducationService educationService;
    private final ProjectService projectService;

    // 스펙 전체 조회
    @GetMapping("/all")
    public ApiResponse<SpecResponseDTO> getAllSpecs(@AuthenticationPrincipal Long userId) {
        UserResponseDTO user = UserResponseDTO.from(userService.getUserById(userId));

        SpecResponseDTO specs = new SpecResponseDTO(
                user,
                activityService.getActivities(userId),
                careerService.getCareers(userId),
                certificateService.getCertificates(userId),
                educationService.getEducations(userId),
                projectService.getProjects(userId)
        );

        return ApiResponse.onSuccess("전체 스펙 조회 성공", specs);
    }
}
