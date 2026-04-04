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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
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

        String prompt = buildPrompt(educations, careers, certificates, projects, activities, algorithm, preference);
        AnalyticResponseDTO result = geminiService.analyze(prompt);

        analyticRepository.save(Analytic.builder()
                .user(user)
                .overallScore(result.overallScore())
                .categoryScores(result.categoryScores())
                .strengths(result.strengths())
                .improvements(result.improvements())
                .summary(result.summary())
                .build());

        return result;
    }

    private String buildPrompt(
            List<EducationResponseDTO> educations,
            List<CareerResponseDTO> careers,
            List<CertificateResponseDTO> certificates,
            List<ProjectResponseDTO> projects,
            List<ActivityResponseDTO> activities,
            AlgorithmResponseDTO algorithm,
            CareerPreference preference) {

        StringBuilder sb = new StringBuilder();

        sb.append("[역할]\n")
                .append("당신은 한국 IT/SW 채용 시장에서 10년 이상 경력을 가진 취업 포트폴리오 전문 컨설턴트입니다.\n")
                .append("평가 대상은 컴퓨터공학/소프트웨어 관련 전공 대학생 또는 신입 구직자입니다.\n\n");

        // 희망 진로
        sb.append("[희망 진로]\n");
        if (preference != null) {
            sb.append(String.format("- 희망 직무: %s\n", preference.getJobRole()));
            sb.append(String.format("- 선호 기업 유형: %s\n", preference.getCompanyType()));
            sb.append("※ 반드시 위 희망 직무와 선호 기업 유형을 기준으로 평가하세요.\n");
            sb.append("  - 해당 직무에 필요한 기술스택/경험이 포트폴리오에 있는지 중점 평가\n");
            sb.append("  - 해당 기업 유형의 채용 기준에 맞춰 점수 산정\n");
            sb.append("  - strengths/improvements도 이 직무 관점에서 작성\n");
        } else {
            sb.append("- 미입력 (일반 IT/SW 신입 채용 기준으로 평가)\n");
        }

        sb.append("\n[평가 원칙]\n")
                .append("1. 한국 IT 기업 신입 채용 기준으로 평가하되, 위 희망 진로를 최우선으로 반영하세요.\n")
                .append("2. 신입/주니어 특성을 반영하세요. 경력이 없어도 프로젝트, 코딩역량 등으로 충분히 높은 점수를 받을 수 있습니다.\n")
                .append("3. 데이터가 없는 항목은 '미입력'으로 간주하고, 해당 항목만 낮게 평가하되 전체 평가에 과도한 패널티를 주지 마세요.\n")
                .append("4. 양보다 질을 중시하되, 일정량 이상의 경험도 함께 고려하세요.\n")
                .append("5. 각 카테고리를 독립적으로 평가한 뒤 가중 평균으로 전체 점수를 산출하세요.\n\n")
                .append("[포트폴리오 데이터]\n\n");

        // 학력
        sb.append("## 학력\n");
        if (educations != null && !educations.isEmpty()) {
            EducationResponseDTO edu = educations.get(0);
            sb.append(String.format("- 학교명: %s\n", nvl(edu.schoolName())));
            sb.append(String.format("- 전공: %s\n", nvl(edu.major())));
            sb.append(String.format("- 학위: %s\n", nvl(edu.degree())));
            sb.append(String.format("- 재학 상태: %s\n", nvl(edu.status())));
            if (edu.gpa() != null && edu.maxGpa() != null) {
                sb.append(String.format("- 학점: %.2f / %.2f\n", edu.gpa(), edu.maxGpa()));
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
                CareerResponseDTO c = careers.get(i);
                sb.append(String.format("%d. [%s] %s | %s | %s ~ %s\n",
                        i + 1,
                        nvl(c.type()),
                        nvl(c.company()),
                        nvl(c.position()),
                        nvl(c.startDate()),
                        nvl(c.endDate())));
                if (c.description() != null && !c.description().isBlank()) {
                    sb.append(String.format("   - %s\n", c.description()));
                }
            }
        }

        // 자격증
        sb.append(String.format("\n## 자격증 (%d건)\n", certificates.size()));
        if (certificates.isEmpty()) {
            sb.append("- 입력된 자격증 없음\n");
        } else {
            for (int i = 0; i < certificates.size(); i++) {
                CertificateResponseDTO cert = certificates.get(i);
                sb.append(String.format("%d. %s | 종류: %s | 발급기관: %s | 취득일: %s",
                        i + 1,
                        nvl(cert.name()),
                        nvl(cert.type()),
                        nvl(cert.issuer()),
                        nvl(cert.issueDate())));
                if (cert.score() != null && !cert.score().isBlank()) {
                    sb.append(String.format(" | 점수/등급: %s", cert.score()));
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
                ProjectResponseDTO p = projects.get(i);
                sb.append(String.format("%d. [%s] %s | 역할: %s | %s ~ %s\n",
                        i + 1,
                        nvl(p.type()),
                        nvl(p.name()),
                        nvl(p.role()),
                        nvl(p.startDate()),
                        nvl(p.endDate())));
                if (p.techStack() != null && !p.techStack().isBlank()) {
                    sb.append(String.format("   - 기술 스택: %s\n", p.techStack()));
                }
                if (p.githubLink() != null && !p.githubLink().isBlank()) {
                    sb.append(String.format("   - GitHub: %s\n", p.githubLink()));
                }
                if (p.demoLink() != null && !p.demoLink().isBlank()) {
                    sb.append(String.format("   - Demo: %s\n", p.demoLink()));
                }
                if (p.description() != null && !p.description().isBlank()) {
                    sb.append(String.format("   - 설명: %s\n", p.description()));
                }
            }
        }

        // 대외활동
        sb.append(String.format("\n## 대외활동 (%d건)\n", activities.size()));
        if (activities.isEmpty()) {
            sb.append("- 입력된 대외활동 없음\n");
        } else {
            for (int i = 0; i < activities.size(); i++) {
                ActivityResponseDTO a = activities.get(i);
                sb.append(String.format("%d. %s | 유형: %s | 역할: %s | %s ~ %s\n",
                        i + 1,
                        nvl(a.name()),
                        nvl(a.type()),
                        nvl(a.role()),
                        nvl(a.startMonth()),
                        nvl(a.endMonth())));
                if (a.result() != null && !a.result().isBlank()) {
                    sb.append(String.format("   - 결과: %s\n", a.result()));
                }
            }
        }

        // 코딩테스트
        sb.append("\n## 코딩테스트 역량 (BOJ/solved.ac)\n");
        if (algorithm != null) {
            sb.append(String.format("- 핸들: %s\n", nvl(algorithm.bojHandle())));
            sb.append(String.format("- 티어: %s\n", tierName(algorithm.tier())));
            sb.append(String.format("- 해결 문제 수: %d\n", nullSafe(algorithm.solvedCount())));
            sb.append(String.format("- 레이팅: %d\n", nullSafe(algorithm.rating())));
        } else {
            sb.append("- 입력된 코딩테스트 정보 없음\n");
        }

        // 채점 기준
        sb.append("\n[카테고리별 평가 루브릭]\n\n");

        sb.append("### 1. 실무경력 (가중치 20%)\n");
        sb.append("90~100: 대기업/유명 스타트업 정규직 6개월+, 희망 직무 관련 핵심 업무 수행\n");
        sb.append("70~89: IT 기업 인턴 3개월+ 또는 스타트업 실무 경험, 관련 직무\n");
        sb.append("50~69: 단기 인턴(1~3개월), IT 관련 아르바이트 경험\n");
        sb.append("30~49: 비IT 직종 경험 또는 프리랜서 소규모 작업\n");
        sb.append("10~29: 간접 경험만 존재 (교내 근로, 비관련 아르바이트)\n");
        sb.append("0~9: 경력 없음\n");
        sb.append("※ 신입 기준 경력이 없어도 정상입니다. 0점이어도 다른 항목으로 충분히 보완 가능하며, 전체 점수에 과도한 패널티를 주지 마세요.\n\n");

        sb.append("### 2. 프로젝트경험 (가중치 25%)\n");
        sb.append("90~100: 5건+, 희망 직무 관련 깊이 있는 프로젝트, 실서비스 배포/운영, GitHub 활발\n");
        sb.append("70~89: 3~4건, 관련 기술스택 경험, GitHub 링크 보유, 팀 프로젝트 포함\n");
        sb.append("50~69: 2건, 기본 기술스택 활용, 학과 과제 프로젝트 위주\n");
        sb.append("30~49: 1건, 단순 구현 수준, 문서화/링크 부족\n");
        sb.append("0~29: 프로젝트 없음\n");
        sb.append("※ 희망 직무와 관련된 기술스택 사용 시 가산. GitHub/Demo 링크 보유 시 추가 가산.\n\n");

        sb.append("### 3. 자격증/어학 (가중치 15%)\n");
        sb.append("90~100: 정보처리기사 + TOEIC 900+(또는 OPIc AL/IH) + 추가 전문 자격증(AWS, SQLD 등)\n");
        sb.append("70~89: 정보처리기사 + TOEIC 800+, 또는 전문 클라우드/DB 자격증 보유\n");
        sb.append("50~69: TOEIC 700~799 또는 기사급 자격증 1개\n");
        sb.append("30~49: TOEIC 600~699 또는 산업기사/기능사급\n");
        sb.append("10~29: 관련성 낮은 자격증만 보유\n");
        sb.append("0~9: 자격증/어학 없음\n");
        sb.append("※ 희망 직무 관련 자격증(예: 백엔드→SQLD, 클라우드→AWS SAA) 보유 시 추가 가산.\n\n");

        sb.append("### 4. 학점/전공 (가중치 15%)\n");
        sb.append("90~100: 4.0/4.5 이상, CS/SW 관련 전공, 졸업 또는 졸업예정\n");
        sb.append("75~89: 3.5~3.99/4.5, 관련 전공\n");
        sb.append("60~74: 3.0~3.49/4.5, 관련 전공. 또는 3.5+ 비관련 전공이나 복수전공/부전공 이수\n");
        sb.append("40~59: 2.5~2.99/4.5\n");
        sb.append("20~39: 2.5 미만\n");
        sb.append("0~19: 학력 미입력\n\n");

        sb.append("### 5. 대외활동 (가중치 10%)\n");
        sb.append("90~100: IT 공모전 수상 + 유명 부트캠프 수료 + 커뮤니티/오픈소스 기여\n");
        sb.append("70~89: IT 공모전 수상 또는 부트캠프/교육 프로그램 수료\n");
        sb.append("50~69: IT 동아리, 스터디 그룹, 해커톤 참여\n");
        sb.append("30~49: 일반 동아리, 봉사활동, 학생회\n");
        sb.append("10~29: 단순 참여 수준의 활동\n");
        sb.append("0~9: 대외활동 없음\n\n");

        sb.append("### 6. 코딩역량 (가중치 15%)\n");
        sb.append("solved.ac 티어 기준:\n");
        sb.append("90~100: Ruby(31+), 해결 1000+, 레이팅 2500+\n");
        sb.append("80~89: Diamond(26~30), 해결 700+, 레이팅 2000+\n");
        sb.append("65~79: Platinum(21~25), 해결 500+, 레이팅 1600+\n");
        sb.append("50~64: Gold(16~20), 해결 300+\n");
        sb.append("35~49: Silver(11~15), 해결 100+\n");
        sb.append("15~34: Bronze(6~10)\n");
        sb.append("0~14: Unrated 또는 미등록\n");
        sb.append("※ Gold 이상이면 대부분의 기업 코딩테스트를 무난히 통과할 수 있는 수준입니다. Gold를 과소평가하지 마세요.\n\n");

        sb.append("[가중 평균 공식]\n");
        sb.append("overallScore = 실무경력×0.20 + 프로젝트경험×0.25 + 자격증어학×0.15 + 학점전공×0.15 + 대외활동×0.10 + 코딩역량×0.15\n");
        sb.append("(소수점 반올림하여 정수로)\n\n");

        sb.append("[응답 지침]\n");
        sb.append("1. 각 카테고리를 위 루브릭에 따라 독립적으로 채점하세요.\n");
        sb.append("2. strengths는 이 포트폴리오의 구체적인 강점 3개를 희망 직무 관점에서 서술하세요.\n");
        sb.append("3. improvements는 희망 직무 취업을 위한 실질적 조언 3개를 서술하세요.\n");
        sb.append("4. summary는 희망 직무 기준으로 전체 포트폴리오를 2~3문장으로 종합 평가하세요.\n");
        sb.append("5. 반드시 아래 JSON 형식으로만 응답하세요. JSON 외 텍스트를 포함하지 마세요.\n\n");

        sb.append("{\n");
        sb.append("  \"overallScore\": 정수(0~100),\n");
        sb.append("  \"categoryScores\": {\n");
        sb.append("    \"실무경력\": 정수,\n");
        sb.append("    \"프로젝트경험\": 정수,\n");
        sb.append("    \"자격증어학\": 정수,\n");
        sb.append("    \"학점전공\": 정수,\n");
        sb.append("    \"대외활동\": 정수,\n");
        sb.append("    \"코딩역량\": 정수\n");
        sb.append("  },\n");
        sb.append("  \"strengths\": [\"구체적 강점1\", \"구체적 강점2\", \"구체적 강점3\"],\n");
        sb.append("  \"improvements\": [\"구체적 보완점1\", \"구체적 보완점2\", \"구체적 보완점3\"],\n");
        sb.append("  \"summary\": \"종합 평가 2~3문장\"\n");
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
