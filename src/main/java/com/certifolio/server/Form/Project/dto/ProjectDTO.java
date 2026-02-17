package com.certifolio.server.Form.Project.dto;

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
    private String teamSize;
    private String techStack;
    private String description;
    private String githubLink;
    private String demoLink;
    private String result;
    private String startDate;
    private String endDate;
}
