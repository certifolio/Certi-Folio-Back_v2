package com.certifolio.server.domain.portfolio.service;

import com.certifolio.server.domain.form.activity.entity.Activity;
import com.certifolio.server.domain.form.career.entity.Career;
import com.certifolio.server.domain.form.project.entity.Project;
import com.certifolio.server.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PortfolioPromptBuilder {

    public String build(User user, List<Career> careers, List<Project> projects, List<Activity> activities) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 한국 취업 포트폴리오 작성 전문가입니다.\n");
        sb.append("아래 사용자 스펙 데이터를 바탕으로 포트폴리오 각 섹션을 작성해주세요.\n");
        sb.append("제공되지 않은 섹션의 출력 키는 응답에서 생략하거나 빈 배열로 두세요.\n\n");

        // ----- 규칙 (제공된 섹션만) -----
        sb.append("## 규칙\n");
        sb.append("1. introductions: 제공된 정보를 종합해 3개의 소제목+문단(2~3문장)으로 작성. 구체적 기술명 포함. 정보가 부족해도 일단 그럴듯하게 작성.\n");

        List<String> rules = new ArrayList<>();
        if (!careers.isEmpty()) {
            rules.add("careerAchievements: 각 경력별 [문제→해결→결과] 카드 1~3개. 구체적 수치 포함. careerIndex는 아래 입력 순서.");
        }
        if (!projects.isEmpty()) {
            rules.add("projectAchievements: 각 프로젝트별로 다음을 생성:\n"
                    + "   - subtitle: 프로젝트 한 줄 소개 (DB 입력값과 별개로 새로 작성)\n"
                    + "   - description: 프로젝트 개요 설명 2~3문장 (DB 입력값을 다듬어서)\n"
                    + "   - achievements: [문제→해결→결과] 카드 정확히 3개\n"
                    + "   - 정보가 부족해도 일단 그럴듯하게 작성하라. 사용자가 이후 직접 수정함.\n"
                    + "   - projectIndex는 아래 입력 순서.");
        }
        if (!activities.isEmpty()) {
            rules.add("activityBullets: 각 활동을 1~2줄 bullet로 요약. activityIndex는 아래 입력 순서.");
        }
        for (int i = 0; i < rules.size(); i++) {
            sb.append(i + 2).append(". ").append(rules.get(i)).append("\n");
        }
        sb.append(rules.size() + 2).append(". 어조: 전문적, 간결, 성과 중심, '~했습니다' 체.\n\n");

        // ----- 사용자 -----
        sb.append("## 사용자\n");
        sb.append("이름: ").append(user.getName()).append("\n\n");

        // ----- 데이터 (있는 것만) -----
        if (!careers.isEmpty()) {
            sb.append("## 경력 (careerIndex 순)\n");
            for (int i = 0; i < careers.size(); i++) {
                Career c = careers.get(i);
                sb.append(String.format("[%d] %s | %s | %s ~ %s\n  설명: %s\n",
                        i, c.getCompany(), c.getType(),
                        c.getStartDate(), c.getEndDate(), c.getDescription()));
            }
            sb.append("\n");
        }

        if (!projects.isEmpty()) {
            sb.append("## 프로젝트 (projectIndex 순)\n");
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                sb.append(String.format("[%d] %s | %s | techStack: %s\n  설명: %s\n  결과: %s\n",
                        i, p.getName(), p.getType(), p.getTechStack(),
                        p.getDescription(), p.getResult() == null ? "" : p.getResult()));
            }
            sb.append("\n");
        }

        if (!activities.isEmpty()) {
            sb.append("## 활동 (activityIndex 순)\n");
            for (int i = 0; i < activities.size(); i++) {
                Activity a = activities.get(i);
                sb.append(String.format("[%d] %s | %s\n  설명: %s\n  성과: %s\n",
                        i, a.getName(), a.getType(),
                        a.getDescription(), a.getResult() == null ? "" : a.getResult()));
            }
            sb.append("\n");
        }

        // ----- 응답 스키마 (제공된 섹션만) -----
        sb.append("## 응답 형식 (반드시 이 JSON만 반환)\n");
        sb.append("{\n");
        sb.append("  \"introductions\": [{\"title\":\"\",\"content\":\"\"}]");
        if (!careers.isEmpty()) {
            sb.append(",\n  \"careerAchievements\": [{\"careerIndex\":0,\"achievements\":[{\"title\":\"\",\"problem\":\"\",\"solution\":\"\",\"result\":\"\"}]}]");
        }
        if (!projects.isEmpty()) {
            sb.append(",\n  \"projectAchievements\": [{\"projectIndex\":0,\"subtitle\":\"\",\"description\":\"\",\"achievements\":[{\"title\":\"\",\"problem\":\"\",\"solution\":\"\",\"result\":\"\"}]}]");
        }
        if (!activities.isEmpty()) {
            sb.append(",\n  \"activityBullets\": [{\"activityIndex\":0,\"bullets\":[\"\"]}]");
        }
        sb.append("\n}\n");

        return sb.toString();
    }
}
