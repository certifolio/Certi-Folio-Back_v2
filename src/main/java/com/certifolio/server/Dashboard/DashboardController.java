package com.certifolio.server.Dashboard;

import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Education.service.EducationService;
import com.certifolio.server.Form.Project.service.ProjectService;
import com.certifolio.server.Form.Activity.service.ActivityService;
import com.certifolio.server.Form.Certificate.service.CertificateService;
import com.certifolio.server.Form.Career.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EducationService educationService;
    private final ProjectService projectService;
    private final ActivityService activityService;
    private final CertificateService certificateService;
    private final CareerService careerService;
    private final UserRepository userRepository;

    private Long getUserId(Object principal) {
        String subject = principal.toString();
        if (subject.contains(":")) {
            String[] parts = subject.split(":");
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).map(User::getId).orElseThrow();
        } else {
            return userRepository.findByEmail(subject).map(User::getId).orElseThrow();
        }
    }

    @GetMapping
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal Object principal) {
        Long userId = getUserId(principal);
        EducationDTO education = educationService.getEducation(userId);

        // Gather all portfolio data for dashboard
        Map<String, Object> dashboard = new HashMap<>();

        var certificates = certificateService.getCertificates(userId);
        var projects = projectService.getProjects(userId);
        var activities = activityService.getActivities(userId);
        var careers = careerService.getCareers(userId);
        var educations = education != null ? singletonList(education) : emptyList();

        // Summary stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("certificateCount", certificates.size());
        stats.put("projectCount", projects.size());
        stats.put("activityCount", activities.size());
        stats.put("careerCount", careers.size());
        stats.put("educationCount", educations.size());

        // Skill progress placeholder (to be calculated from actual data)
        dashboard.put("stats", stats);
        dashboard.put("certificates", certificates);
        dashboard.put("projects", projects);
        dashboard.put("activities", activities);
        dashboard.put("careers", careers);
        dashboard.put("educations", educations);
        dashboard.put("hasPortfolioData", !certificates.isEmpty() || !projects.isEmpty() || !activities.isEmpty()
                || !careers.isEmpty() || !educations.isEmpty());

        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshDashboard(@AuthenticationPrincipal Object principal) {
        // Same as GET for now
        return getDashboard(principal);
    }
}
