package com.certifolio.server.Analytics.service;

import com.certifolio.server.Analytics.dto.AnalyticsResultDTO;
import com.certifolio.server.Form.Activity.dto.ActivityDTO;
import com.certifolio.server.Form.Activity.service.ActivityService;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Career.service.CareerService;
import com.certifolio.server.Form.Certificate.dto.CertificateDTO;
import com.certifolio.server.Form.Certificate.service.CertificateService;
import com.certifolio.server.Form.CodingTest.dto.CodingTestDTO;
import com.certifolio.server.Form.CodingTest.service.CodingTestService;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.Education.service.EducationService;
import com.certifolio.server.Form.Project.dto.ProjectDTO;
import com.certifolio.server.Form.Project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private final ActivityService activityService;
    private final CareerService careerService;
    private final CertificateService certificateService;
    private final CodingTestService codingTestService;
    private final EducationService educationService;
    private final ProjectService projectService;
    private final GeminiService geminiService;

    public AnalyticsResultDTO analyzePortfolio(Long userId) {
        List<ActivityDTO> activities = activityService.getActivities(userId);
        List<CareerDTO> careers = careerService.getCareers(userId);
        List<CertificateDTO> certificates = certificateService.getCertificates(userId);
        CodingTestDTO codingTest = codingTestService.getCodingTest(userId);
        EducationDTO education = educationService.getEducation(userId);
        List<ProjectDTO> projects = projectService.getProjects(userId);

        String prompt = buildPrompt(education, careers, certificates, projects, activities, codingTest);
        return geminiService.analyze(prompt);
    }

    private String buildPrompt(
            EducationDTO education,
            List<CareerDTO> careers,
            List<CertificateDTO> certificates,
            List<ProjectDTO> projects,
            List<ActivityDTO> activities,
            CodingTestDTO codingTest) {

        StringBuilder sb = new StringBuilder();

        sb.append("당신은 취업 포트폴리오 전문 분석가입니다. ")
          .append("아래 사용자의 포트폴리오 데이터를 분석하여 취업 경쟁력을 평가해주세요.\n\n");

        // 학력
        sb.append("## 학력\n");
        if (education != null) {
            sb.append(String.format("- 학교명: %s\n", nvl(education.getSchoolName())));
            sb.append(String.format("- 전공: %s\n", nvl(education.getMajor())));
            sb.append(String.format("- 학위: %s\n", nvl(education.getDegree())));
            sb.append(String.format("- 재학 상태: %s\n", nvl(education.getStatus())));
            if (education.getGpa() != null && education.getMaxGpa() != null) {
                sb.append(String.format("- 학점: %.2f / %.2f\n", education.getGpa(), education.getMaxGpa()));
            }
        } else {
            sb.append("- 입력된 학력 없음\n");
        }

        // 경력
        sb.append(String.format("\n## 경력 (%d건)\n", careers.size()));
        if (careers.isEmpty()) {
            sb.append("- 입력된 경력 없음\n");
        } else {
            for (int i = 0; i < careers.size(); i++) {
                CareerDTO c = careers.get(i);
                sb.append(String.format("%d. [%s] %s | %s | %s ~ %s\n",
                        i + 1,
                        nvl(c.getType()),
                        nvl(c.getCompany()),
                        nvl(c.getPosition()),
                        nvl(c.getStartDate()),
                        nvl(c.getEndDate())));
                if (c.getDescription() != null && !c.getDescription().isBlank()) {
                    sb.append(String.format("   - %s\n", c.getDescription()));
                }
            }
        }

        // 자격증
        sb.append(String.format("\n## 자격증 (%d건)\n", certificates.size()));
        if (certificates.isEmpty()) {
            sb.append("- 입력된 자격증 없음\n");
        } else {
            for (int i = 0; i < certificates.size(); i++) {
                CertificateDTO cert = certificates.get(i);
                sb.append(String.format("%d. %s | 종류: %s | 발급기관: %s | 취득일: %s",
                        i + 1,
                        nvl(cert.getName()),
                        nvl(cert.getType()),
                        nvl(cert.getIssuer()),
                        nvl(cert.getIssueDate())));
                if (cert.getScore() != null && !cert.getScore().isBlank()) {
                    sb.append(String.format(" | 점수/등급: %s", cert.getScore()));
                }
                sb.append("\n");
            }
        }

        // 프로젝트
        sb.append(String.format("\n## 프로젝트 (%d건)\n", projects.size()));
        if (projects.isEmpty()) {
            sb.append("- 입력된 프로젝트 없음\n");
        } else {
            for (int i = 0; i < projects.size(); i++) {
                ProjectDTO p = projects.get(i);
                sb.append(String.format("%d. [%s] %s | 역할: %s | %s ~ %s\n",
                        i + 1,
                        nvl(p.getType()),
                        nvl(p.getName()),
                        nvl(p.getRole()),
                        nvl(p.getStartDate()),
                        nvl(p.getEndDate())));
                if (p.getTechStack() != null && !p.getTechStack().isBlank()) {
                    sb.append(String.format("   - 기술 스택: %s\n", p.getTechStack()));
                }
                if (p.getGithubLink() != null && !p.getGithubLink().isBlank()) {
                    sb.append(String.format("   - GitHub: %s\n", p.getGithubLink()));
                }
                if (p.getDemoLink() != null && !p.getDemoLink().isBlank()) {
                    sb.append(String.format("   - Demo: %s\n", p.getDemoLink()));
                }
                if (p.getDescription() != null && !p.getDescription().isBlank()) {
                    sb.append(String.format("   - 설명: %s\n", p.getDescription()));
                }
            }
        }

        // 대외활동
        sb.append(String.format("\n## 대외활동 (%d건)\n", activities.size()));
        if (activities.isEmpty()) {
            sb.append("- 입력된 대외활동 없음\n");
        } else {
            for (int i = 0; i < activities.size(); i++) {
                ActivityDTO a = activities.get(i);
                sb.append(String.format("%d. %s | 유형: %s | 역할: %s | %s ~ %s\n",
                        i + 1,
                        nvl(a.getName()),
                        nvl(a.getType()),
                        nvl(a.getRole()),
                        nvl(a.getStartDate()),
                        nvl(a.getEndDate())));
                if (a.getResult() != null && !a.getResult().isBlank()) {
                    sb.append(String.format("   - 결과: %s\n", a.getResult()));
                }
            }
        }

        // 코딩테스트
        sb.append("\n## 코딩테스트 역량 (BOJ/solved.ac)\n");
        if (codingTest != null) {
            sb.append(String.format("- 핸들: %s\n", nvl(codingTest.getBojHandle())));
            sb.append(String.format("- 티어: %s\n", tierName(codingTest.getTier())));
            sb.append(String.format("- 해결 문제 수: %d\n", nullSafe(codingTest.getSolvedCount())));
            sb.append(String.format("- 레이팅: %d\n", nullSafe(codingTest.getRating())));
            sb.append(String.format("- 최대 스트릭: %d일\n", nullSafe(codingTest.getMaxStreak())));
        } else {
            sb.append("- 입력된 코딩테스트 정보 없음\n");
        }

        // 채점 기준
        sb.append("\n[채점 기준]\n");
        sb.append("- 실무경력 (0~100): 경력 건수와 총 기간, 직무 유형(정규직>인턴>기타), 상세 업무 내용\n");
        sb.append("- 프로젝트경험 (0~100): 프로젝트 수, 기술 스택 다양성, GitHub/Demo 링크 보유 여부, 팀/개인 균형\n");
        sb.append("- 자격증어학 (0~100): IT 자격증 수와 수준, 어학 점수(TOEIC 800+→70점대, 900+→90점대, OPIc AL→95+)\n");
        sb.append("- 학점전공 (0~100): 학점 수준(4.5기준 4.0+→90점대, 3.5+→75점대, 3.0+→60점대), 전공 관련성\n");
        sb.append("- 대외활동 (0~100): 활동 건수, 종류(공모전수상>부트캠프/교육>동아리>봉사), 역할과 성과\n");
        sb.append("- 어학역량 (0~100): BOJ 티어(Gold→40점대, Platinum→60점대, Diamond→80점대, Ruby→95+), 해결 문제 수, 레이팅\n\n");

        sb.append("전체 점수(overallScore)는 가중 평균: 실무경력(25%) + 프로젝트경험(25%) + 자격증어학(15%) + 학점전공(15%) + 대외활동(10%) + 어학역량(10%)\n\n");

        sb.append("아래 JSON 형식으로만 응답하세요:\n");
        sb.append("{\n");
        sb.append("  \"overallScore\": 정수(0~100),\n");
        sb.append("  \"categoryScores\": {\n");
        sb.append("    \"실무경력\": 정수,\n");
        sb.append("    \"프로젝트경험\": 정수,\n");
        sb.append("    \"자격증어학\": 정수,\n");
        sb.append("    \"학점전공\": 정수,\n");
        sb.append("    \"대외활동\": 정수,\n");
        sb.append("    \"어학역량\": 정수\n");
        sb.append("  },\n");
        sb.append("  \"strengths\": [\"강점1\", \"강점2\", \"강점3\"],\n");
        sb.append("  \"improvements\": [\"보완점1\", \"보완점2\", \"보완점3\"],\n");
        sb.append("  \"summary\": \"전체 포트폴리오 종합 평가 (2~3문장)\"\n");
        sb.append("}");

        return sb.toString();
    }

    private String nvl(String value) {
        return value != null ? value : "-";
    }

    private int nullSafe(Integer value) {
        return value != null ? value : 0;
    }

    private String tierName(Integer tier) {
        if (tier == null) return "미입력";
        if (tier == 0) return "언레이티드";
        String[] tierGroups = {"Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby"};
        String[] levels = {"V", "IV", "III", "II", "I"};
        int groupIndex = (tier - 1) / 5;
        int levelIndex = (tier - 1) % 5;
        if (groupIndex >= tierGroups.length) return "Ruby I";
        return tierGroups[groupIndex] + " " + levels[levelIndex];
    }
}
