package com.certifolio.server.domain.form.project.dto.response;

import com.certifolio.server.domain.form.project.entity.Project;
import com.certifolio.server.global.common.util.DateUtils;

public record ProjectResponseDTO(
        Long id,
        String name,
        String type,
        String role,
        String techStack,
        String description,
        String githubLink,
        String demoLink,
        String result,
        String startDate,
        String endDate
) {
    public static ProjectResponseDTO from(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getType(),
                project.getRole(),
                project.getTechStack(),
                project.getDescription(),
                project.getGithubLink(),
                project.getDemoLink(),
                project.getResult(),
                DateUtils.dateToString(project.getStartDate()),
                DateUtils.dateToString(project.getEndDate())
        );
    }
}
