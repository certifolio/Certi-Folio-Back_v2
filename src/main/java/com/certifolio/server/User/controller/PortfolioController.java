package com.certifolio.server.User.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.dto.*;
import com.certifolio.server.Activity.dto.ActivityDTO;
import com.certifolio.server.Career.dto.CareerDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.User.service.PortfolioServiceImpl;
import com.certifolio.server.auth.util.AuthUtils;
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

    private Long getUserId(Object principal) {
        User user = AuthUtils.resolveUser(principal, userRepository);
        if (user == null) {
            throw new RuntimeException("인증이 필요합니다.");
        }
        return user.getId();
    }

    // ===== CERTIFICATES =====

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

    @PutMapping("/certificates/{id}")
    public ResponseEntity<?> updateCertificate(@AuthenticationPrincipal Object principal,
            @PathVariable Long id, @RequestBody CertificateDTO dto) {
        CertificateDTO updated = portfolioService.updateCertificate(getUserId(principal), id, dto);
        return ResponseEntity.ok(Map.of("success", true, "certificate", updated));
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<?> deleteCertificate(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteCertificate(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== PROJECTS =====

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

    @PutMapping("/projects/{id}")
    public ResponseEntity<?> updateProject(@AuthenticationPrincipal Object principal,
            @PathVariable Long id, @RequestBody ProjectDTO dto) {
        ProjectDTO updated = portfolioService.updateProject(getUserId(principal), id, dto);
        return ResponseEntity.ok(Map.of("success", true, "project", updated));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteProject(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== ACTIVITIES =====

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

    @PutMapping("/activities/{id}")
    public ResponseEntity<?> updateActivity(@AuthenticationPrincipal Object principal,
            @PathVariable Long id, @RequestBody ActivityDTO dto) {
        ActivityDTO updated = portfolioService.updateActivity(getUserId(principal), id, dto);
        return ResponseEntity.ok(Map.of("success", true, "activity", updated));
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<?> deleteActivity(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteActivity(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== CAREERS =====

    @PostMapping("/careers")
    public ResponseEntity<?> saveCareers(@AuthenticationPrincipal Object principal, @RequestBody List<CareerDTO> dtos) {
        portfolioService.saveCareers(getUserId(principal), dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/careers")
    public ResponseEntity<?> getCareers(@AuthenticationPrincipal Object principal) {
        Long userId = getUserId(principal);
        List<CareerDTO> careers = portfolioService.getCareers(userId);

        int total = careers.size();
        int current = (int) careers.stream().filter(CareerDTO::isCurrent).count();
        int companies = (int) careers.stream().map(CareerDTO::getCompany).distinct().count();

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
                    totalMonths += (int) Math.max(1, months);
                }
            } catch (Exception e) {
                // Skip if date parsing fails
            }
        }

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("current", current);
        stats.put("totalMonths", totalMonths);
        stats.put("companies", companies);

        return ResponseEntity.ok(Map.of("success", true, "careers", careers, "stats", stats));
    }

    @PutMapping("/careers/{id}")
    public ResponseEntity<?> updateCareer(@AuthenticationPrincipal Object principal,
            @PathVariable Long id, @RequestBody CareerDTO dto) {
        CareerDTO updated = portfolioService.updateCareer(getUserId(principal), id, dto);
        return ResponseEntity.ok(Map.of("success", true, "career", updated));
    }

    @DeleteMapping("/careers/{id}")
    public ResponseEntity<?> deleteCareer(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteCareer(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Helper to parse various date formats
    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return java.time.LocalDate.now();
        }
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return java.time.LocalDate.parse(dateStr);
        }
        if (dateStr.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return java.time.LocalDate.parse(dateStr.replace(".", "-"));
        }
        if (dateStr.matches("\\d{4}-\\d{2}")) {
            return java.time.LocalDate.parse(dateStr + "-01");
        }
        if (dateStr.matches("\\d{4}\\.\\d{2}")) {
            return java.time.LocalDate.parse(dateStr.replace(".", "-") + "-01");
        }
        return java.time.LocalDate.now();
    }

    // ===== EDUCATIONS =====

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

    @PutMapping("/educations/{id}")
    public ResponseEntity<?> updateEducation(@AuthenticationPrincipal Object principal,
            @PathVariable Long id, @RequestBody EducationDTO dto) {
        EducationDTO updated = portfolioService.updateEducation(getUserId(principal), id, dto);
        return ResponseEntity.ok(Map.of("success", true, "education", updated));
    }

    @DeleteMapping("/educations/{id}")
    public ResponseEntity<?> deleteEducation(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        portfolioService.deleteEducation(getUserId(principal), id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ===== FULL PROFILE =====

    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@AuthenticationPrincipal Object principal, @RequestBody ProfileUploadDTO dto) {
        Long userId = getUserId(principal);
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
