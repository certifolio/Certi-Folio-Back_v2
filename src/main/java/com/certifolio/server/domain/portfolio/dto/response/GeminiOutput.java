package com.certifolio.server.domain.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiOutput (
        List<Introduction> introductions,
        List<CareerAchievementGroup> careerAchievements,
        List<ProjectAchievementGroup> projectAchievements,
        List<ActivityBulletGroup> activityBullets
){
    public record Introduction(String title, String content){}
    public record Achievement(String title, String problem, String solution, String result){}
    public record CareerAchievementGroup(int careerIndex, List<Achievement> achievements){}
    public record ProjectAchievementGroup(int projectIndex, String subtitle, String description, List<Achievement> achievements){}
    public record ActivityBulletGroup(int activityIndex, List<String> bullets){}
}
