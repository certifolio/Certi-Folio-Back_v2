package com.certifolio.server.domain.portfolio.service;

import com.certifolio.server.domain.form.activity.entity.Activity;
import com.certifolio.server.domain.form.activity.repository.ActivityRepository;
import com.certifolio.server.domain.form.career.entity.Career;
import com.certifolio.server.domain.form.career.repository.CareerRepository;
import com.certifolio.server.domain.form.certificate.entity.Certificate;
import com.certifolio.server.domain.form.certificate.repository.CertificateRepository;
import com.certifolio.server.domain.form.education.entity.Education;
import com.certifolio.server.domain.form.education.repository.EducationRepository;
import com.certifolio.server.domain.form.project.entity.Project;
import com.certifolio.server.domain.form.project.repository.ProjectRepository;
import com.certifolio.server.domain.portfolio.dto.request.PortfolioDraftUpdateRequest;
import com.certifolio.server.domain.portfolio.dto.response.GeminiOutput;
import com.certifolio.server.domain.portfolio.dto.response.PortfolioDraftResponse;
import com.certifolio.server.domain.portfolio.entity.DraftStatus;
import com.certifolio.server.domain.portfolio.entity.PortfolioDraft;
import com.certifolio.server.domain.portfolio.repository.PortfolioDraftRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.service.GeminiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioDraftService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM");

    private final UserService userService;

    private final ActivityRepository activityRepository;
    private final CareerRepository careerRepository;
    private final CertificateRepository certificateRepository;
    private final EducationRepository educationRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioDraftRepository portfolioDraftRepository;

    private final GeminiService geminiService;
    private final PortfolioPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public PortfolioDraftResponse getLatest(Long userId) {
        PortfolioDraft draft = portfolioDraftRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.PORTFOLIO_DRAFT_NOT_FOUND));

        return PortfolioDraftResponse.from(draft);
    }

    @Transactional
    public PortfolioDraftResponse generate(Long userId) {
        User user = userService.getUserById(userId);

        List<Career> careers = careerRepository.findAllByUserIdOrderByEndDateDesc(userId);
        List<Certificate> certificates = certificateRepository.findAllByUserIdOrderByIssueDateDesc(userId);
        Education education = educationRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        List<Activity> activities = activityRepository.findAllByUserIdOrderByEndMonthDesc(userId);
        List<Project> projects = projectRepository.findAllByUserIdOrderByEndDateDesc(userId);

        if (careers.isEmpty() && certificates.isEmpty() && education == null && activities.isEmpty() && projects.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.PORTFOLIO_SPEC_EMPTY);
        }

        String prompt = promptBuilder.build(user, careers, projects, activities);
        GeminiOutput output = geminiService.generate(prompt, GeminiOutput.class);

        Map<String, Object> content = mergeContent(user, careers, certificates, education, activities, projects, output);

        try {
            PortfolioDraft draft = PortfolioDraft.builder()
                    .user(user)
                    .status(DraftStatus.COMPLETED)
                    .draftContent(objectMapper.writeValueAsString(content))
                    .build();

            portfolioDraftRepository.save(draft);

            return PortfolioDraftResponse.from(draft);
        } catch (JsonProcessingException e) {
            throw new BusinessException(GeneralErrorCode.JSON_PARSE_ERROR);
        }
    }

    @Transactional
    public PortfolioDraftResponse update(Long userId, Long draftId, PortfolioDraftUpdateRequest request) {
        PortfolioDraft draft = portfolioDraftRepository.findById(draftId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.PORTFOLIO_DRAFT_NOT_FOUND));

        if (!draft.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.PORTFOLIO_DRAFT_UNAUTHORIZED);
        }

        try {
            draft.update(objectMapper.writeValueAsString(request.content()));
            return PortfolioDraftResponse.from(draft);
        } catch (JsonProcessingException e) {
            throw new BusinessException(GeneralErrorCode.JSON_PARSE_ERROR);
        }
    }

    @Transactional
    public void delete(Long userId, Long draftId) {
        PortfolioDraft draft = portfolioDraftRepository.findById(draftId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.PORTFOLIO_DRAFT_NOT_FOUND));

        if (!draft.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.PORTFOLIO_DRAFT_UNAUTHORIZED);
        }

        portfolioDraftRepository.delete(draft);
    }

    private Map<String, Object> mergeContent(
            User user,
            List<Career> careers,
            List<Certificate> certificates,
            Education education,
            List<Activity> activities,
            List<Project> projects,
            GeminiOutput output
    ) {
        Map<String, Object> content = new LinkedHashMap<>();

        // [사용자 인적사항] - 디비 + 사용자 직접 입력 빈칸
        content.put("name", user.getName());
        content.put("englishName", "");
        content.put("targetRole", "");
        content.put("birthDate", "");
        content.put("phone", "");
        content.put("email", user.getEmail() == null ? "" : user.getEmail());
        content.put("github", "");
        content.put("profileImage", "");

        // [Introduction] - AI
        content.put("introductions", output.introductions() == null ? List.of() : output.introductions());

        // [Skills] - 평면 배열 (모든 프로젝트 techStack 합쳐서 dedupe)
        content.put("skills", buildSkills(projects));

        // [Careers]
        content.put("careers", buildCareers(careers, output));

        // [Projects]
        content.put("projects", buildProjects(projects, output));

        // [Education] - 대학 (고등학교 제외)
        content.put("education", buildEducation(education));

        // [Languages] - 어학 자격증
        content.put("languages", buildLanguages(certificates));

        // [Activities]
        content.put("activities", buildActivities(activities, output));

        // [Awards] - 빈 배열, 프론트에서 직접 추가
        content.put("awards", List.of());

        // [Certificates] - 어학 제외
        content.put("certificates", buildCertificates(certificates));

        return content;
    }

    private List<String> buildSkills(List<Project> projects) {
        return projects.stream()
                .map(Project::getTechStack)
                .filter(Objects::nonNull)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private Map<String, Object> buildEducation(Education edu) {
        if (edu == null) return Map.of();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("school", edu.getSchoolName());
        map.put("major", edu.getMajor());
        map.put("period", formatPeriod(edu.getStartDate(), edu.getEndDate()));
        map.put("status", edu.getStatus());
        map.put("gpa", edu.getGpa() == null ? "" : edu.getGpa());
        map.put("maxGpa", edu.getMaxGpa() == null ? "" : edu.getMaxGpa());
        return map;
    }

    private List<Map<String, Object>> buildProjects(List<Project> projects, GeminiOutput output) {
        Map<Integer, GeminiOutput.ProjectAchievementGroup> aiMap = new HashMap<>();

        if (output.projectAchievements() != null) {
            for (var g : output.projectAchievements()) {
                aiMap.put(g.projectIndex(), g);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            var g = aiMap.get(i);

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("projectName", p.getName());
            map.put("period", formatPeriod(p.getStartDate(), p.getEndDate()));
            map.put("subtitle", g == null ? "" : g.subtitle());           // AI 한 줄 소개
            map.put("description", g == null ? "" : g.description());     // AI 개요 (DB description 노출 X)
            map.put("techStack", splitTechStack(p.getTechStack()));
            map.put("teamSize", p.getType());                             // personal/team
            map.put("links", Map.of(
                    "github", p.getGithubLink() == null ? "" : p.getGithubLink(),
                    "demo", p.getDemoLink() == null ? "" : p.getDemoLink()
            ));
            map.put("achievements", g == null ? List.of() : g.achievements());

            result.add(map);
        }

        return result;
    }

    private List<Map<String, Object>> buildCareers(List<Career> careers, GeminiOutput output) {
        Map<Integer, List<GeminiOutput.Achievement>> aiMap = new HashMap<>();

        if (output.careerAchievements() != null) {
            for (var g : output.careerAchievements()) {
                aiMap.put(g.careerIndex(), g.achievements());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < careers.size(); i++) {
            Career c = careers.get(i);
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("companyName", c.getCompany());
            map.put("period", formatPeriod(c.getStartDate(), c.getEndDate()));
            map.put("type", c.getType());
            map.put("position", c.getPosition() == null ? "" : c.getPosition());
            map.put("description", c.getDescription());
            map.put("achievements", aiMap.getOrDefault(i, List.of()));

            result.add(map);
        }

        return result;
    }

    private List<Map<String, Object>> buildLanguages(List<Certificate> certs) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Certificate c : certs) {
            if (!"language".equalsIgnoreCase(c.getType())) continue;

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", c.getName());
            map.put("score", c.getScore() == null ? "" : c.getScore());
            map.put("date", c.getIssueDate() == null ? "" : c.getIssueDate().format(DATE_FORMATTER));

            result.add(map);
        }

        return result;
    }

    private List<Map<String, Object>> buildCertificates(List<Certificate> certs) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Certificate c : certs) {
            if ("language".equalsIgnoreCase(c.getType())) continue;

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", c.getName());
            map.put("date", c.getIssueDate() == null ? "" : c.getIssueDate().format(DATE_FORMATTER));

            result.add(map);
        }

        return result;
    }

    private List<Map<String, Object>> buildActivities(List<Activity> activities, GeminiOutput output) {
        Map<Integer, List<String>> aiMap = new HashMap<>();

        if (output != null && output.activityBullets() != null) {     // ← null 체크 강화
            for (var g : output.activityBullets()) {
                aiMap.put(g.activityIndex(), g.bullets());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < activities.size(); i++) {
            Activity a = activities.get(i);
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", a.getName());
            map.put("period", formatPeriod(a.getStartMonth(), a.getEndMonth()));
            map.put("bullets", aiMap.getOrDefault(i, List.of()));

            result.add(map);
        }

        return result;
    }


    private List<String> splitTechStack(String techStack) {
        if (techStack == null || techStack.isBlank()) return List.of();
        return Arrays.stream(techStack.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String formatPeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "";
        }

        return start.format(DATE_FORMATTER) + " ~ " + end.format(DATE_FORMATTER);
    }
}
