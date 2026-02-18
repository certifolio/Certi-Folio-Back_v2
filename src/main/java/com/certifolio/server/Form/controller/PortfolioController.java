package com.certifolio.server.Form.controller;

import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.Education.service.EducationService;
import com.certifolio.server.Form.Project.service.ProjectService;
import com.certifolio.server.Form.Activity.service.ActivityService;
import com.certifolio.server.Form.CodingTest.service.CodingTestService;
import com.certifolio.server.Form.Certificate.service.CertificateService;
import com.certifolio.server.Form.Career.service.CareerService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;

/**
 * Portfolio aggregation controller
 * Individual domain endpoints have been separated into:
 * - CertificateController
 * - ProjectController
 * - ActivityController
 * - CareerController
 * - EducationController
 */
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final EducationService educationService;
    private final ProjectService projectService;
    private final ActivityService activityService;
    private final CodingTestService codingTestService;
    private final CertificateService certificateService;
    private final CareerService careerService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Get all portfolio data aggregated
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllPortfolio(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        EducationDTO education = educationService.getEducation(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("certificates", certificateService.getCertificates(userId));
        data.put("projects", projectService.getProjects(userId));
        data.put("activities", activityService.getActivities(userId));
        data.put("codingTest", codingTestService.getCodingTest(userId));
        data.put("careers", careerService.getCareers(userId));
        data.put("educations", education != null ? singletonList(education) : emptyList());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
