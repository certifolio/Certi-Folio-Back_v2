package com.certifolio.server.Form.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Form.dto.ProfileUploadDTO;
import com.certifolio.server.Form.Activity.dto.ActivityDTO;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.Project.dto.ProjectDTO;
import com.certifolio.server.Form.Certificate.dto.CertificateDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.service.PortfolioServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioServiceImpl portfolioService;
    private final UserRepository userRepository;

    // Helper to get User ID
    private Long getUserId(Object principal) {
        // Assuming principal is compatible with how we set it in JwtFilter (String
        // subject)
        // We need to resolve it to ID. Ideally JwtFilter puts UserDetails or ID.
        // Current implementation puts subject (email or provider:id).
        String subject = principal.toString();
        // Since we need ID, we should really look it up or better yet, put ID in token
        // parsing.
        // Let's look up by email or provider/id logic again.
        // This is inefficient, but for now:
        if (subject.contains(":")) {
            String[] parts = subject.split(":");
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).map(User::getId).orElseThrow();
        } else {
            return userRepository.findByEmail(subject).map(User::getId).orElseThrow();
        }
    }

    @PostMapping("/certificates")
    public ResponseEntity<?> saveCertificates(@AuthenticationPrincipal Object principal,
            @RequestBody List<CertificateDTO> dtos) {
        portfolioService.saveCertificates(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/certificates")
    public ResponseEntity<?> getCertificates(@AuthenticationPrincipal Object principal) {
        return ResponseEntity
                .ok(Map.of("success", true, "data", portfolioService.getCertificates(getUserId(principal))));
    }

    @PostMapping("/certificates/add")
    public ResponseEntity<?> addCertificate(@AuthenticationPrincipal Object principal,
            @RequestBody CertificateDTO dto) {
        CertificateDTO saved = portfolioService.addCertificate(getUserId(principal), dto);
        return ResponseEntity.ok(Map.of("success", true, "certificate", saved));
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<?> deleteCertificate(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteCertificate(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/projects")
    public ResponseEntity<?> saveProjects(@AuthenticationPrincipal Object principal,
            @RequestBody List<ProjectDTO> dtos) {
        portfolioService.saveProjects(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getProjects(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(Map.of("success", true, "data", portfolioService.getProjects(getUserId(principal))));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteProject(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/activities")
    public ResponseEntity<?> saveActivities(@AuthenticationPrincipal Object principal,
            @RequestBody List<ActivityDTO> dtos) {
        portfolioService.saveActivities(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(Map.of("success", true, "data", portfolioService.getActivities(getUserId(principal))));
    }

    @PostMapping("/careers")
    public ResponseEntity<?> saveCareers(@AuthenticationPrincipal Object principal, @RequestBody List<CareerDTO> dtos) {
        portfolioService.saveCareers(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/careers")
    public ResponseEntity<?> getCareers(@AuthenticationPrincipal Object principal) {
        Long userId = getUserId(principal);
        List<CareerDTO> careers = portfolioService.getCareers(userId);

        // Calculate stats
        int total = careers.size();
        int current = (int) careers.stream().filter(CareerDTO::isCurrent).count();
        int companies = (int) careers.stream().map(CareerDTO::getCompany).distinct().count();

        // Calculate total months from dates
        int totalMonths = 0;
        for (CareerDTO career : careers) {
            try {
                if (career.getStartDate() != null && !career.getStartDate().isEmpty()) {
                    java.time.LocalDate startDate = parseDate(career.getStartDate());
                    java.time.LocalDate endDate;

                    if (career.isCurrent() || career.getEndDate() == null || career.getEndDate().isEmpty()) {
                        endDate = java.time.LocalDate.now();
                    } else {
                        endDate = parseDate(career.getEndDate());
                    }

                    long months = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
                    totalMonths += (int) Math.max(1, months); // At least 1 month
                }
            } catch (Exception e) {
                // Skip if date parsing fails
                System.out.println("Failed to parse career dates: " + e.getMessage());
            }
        }

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("current", current);
        stats.put("totalMonths", totalMonths);
        stats.put("companies", companies);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "careers", careers,
                "stats", stats));
    }

    // Helper to parse various date formats
    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return java.time.LocalDate.now();
        }

        // Try YYYY-MM-DD format
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return java.time.LocalDate.parse(dateStr);
        }

        // Try YYYY.MM.DD format
        if (dateStr.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return java.time.LocalDate.parse(dateStr.replace(".", "-"));
        }

        // Try YYYY-MM format
        if (dateStr.matches("\\d{4}-\\d{2}")) {
            return java.time.LocalDate.parse(dateStr + "-01");
        }

        // Try YYYY.MM format
        if (dateStr.matches("\\d{4}\\.\\d{2}")) {
            return java.time.LocalDate.parse(dateStr.replace(".", "-") + "-01");
        }

        return java.time.LocalDate.now();
    }

    @PostMapping("/educations")
    public ResponseEntity<?> saveEducations(@AuthenticationPrincipal Object principal,
            @RequestBody List<EducationDTO> dtos) {
        portfolioService.saveEducations(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/educations")
    public ResponseEntity<?> getEducations(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(Map.of("success", true, "data", portfolioService.getEducations(getUserId(principal))));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@AuthenticationPrincipal Object principal, @RequestBody ProfileUploadDTO dto) {
        Long userId = getUserId(principal);

        // Debug logging
        System.out.println("===== PROFILE SAVE REQUEST =====");
        System.out.println("User ID: " + userId);
        System.out.println("HighSchool: " + (dto.getHighSchool() != null ? dto.getHighSchool().getName() : "null"));
        System.out.println("University: " + (dto.getUniversity() != null ? dto.getUniversity().getName() : "null"));
        System.out.println("Projects count: " + (dto.getProjects() != null ? dto.getProjects().size() : 0));
        System.out.println("Activities count: " + (dto.getActivities() != null ? dto.getActivities().size() : 0));
        System.out.println("Certificates count: " + (dto.getCertificates() != null ? dto.getCertificates().size() : 0));
        System.out.println("Experience: " + (dto.getExperience() != null
                ? "Internships="
                        + (dto.getExperience().getInternships() != null ? dto.getExperience().getInternships().size()
                                : 0)
                        +
                        ", Jobs=" + (dto.getExperience().getJobs() != null ? dto.getExperience().getJobs().size() : 0)
                : "null"));
        System.out.println("================================");

        portfolioService.saveFullProfile(userId, dto);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPortfolio(@AuthenticationPrincipal Object principal) {
        Long userId = getUserId(principal);
        Map<String, Object> data = new HashMap<>();
        data.put("certificates", portfolioService.getCertificates(userId));
        data.put("projects", portfolioService.getProjects(userId));
        data.put("activities", portfolioService.getActivities(userId));
        data.put("careers", portfolioService.getCareers(userId));
        data.put("educations", portfolioService.getEducations(userId));
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
