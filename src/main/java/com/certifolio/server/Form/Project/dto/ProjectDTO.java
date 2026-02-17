package com.certifolio.server.Form.Project.dto;

import com.certifolio.server.Form.Project.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String type;
    private String role;
    private String techStack;
    private String description;
    private String githubLink;
    private String demoLink;
    private String result;
    private String startDate;
    private String endDate;
    public static ProjectDTO from(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .type(project.getType())
                .role(project.getRole())
                .techStack(project.getTechStack())
                .description(project.getDescription())
                .githubLink(project.getGithubLink())
                .demoLink(project.getDemoLink())
                .result(project.getResult())
                .startDate(project.getStartDate() != null ? project.getStartDate().toString() : null)
                .endDate(project.getEndDate() != null ? project.getEndDate().toString() : null)
                .build();
    }
}
