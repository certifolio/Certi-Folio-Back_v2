package com.certifolio.server.Form.Project.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String type; // personal, team, etc.

    private String role;

    private String teamSize;

    @Column(columnDefinition = "TEXT")
    private String techStack;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String githubLink;

    private String demoLink;

    private String result;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder
    public Project(User user, String name, String type, String role, String teamSize, String techStack,
            String description, String githubLink, String demoLink, String result, LocalDate startDate,
            LocalDate endDate) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.role = role;
        this.teamSize = teamSize;
        this.techStack = techStack;
        this.description = description;
        this.githubLink = githubLink;
        this.demoLink = demoLink;
        this.result = result;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
