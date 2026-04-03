package com.certifolio.server.domain.form.project.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
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
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // personal, team, etc.

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String techStack;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String githubLink;

    private String demoLink;

    private String result;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public Project(User user, String name, String type, String role, String techStack,
                   String description, String githubLink, String demoLink, String result, LocalDate startDate,
                   LocalDate endDate) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.role = role;
        this.techStack = techStack;
        this.description = description;
        this.githubLink = githubLink;
        this.demoLink = demoLink;
        this.result = result;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(String name, String type, String role, String techStack, String description,
                       String githubLink, String demoLink, String result, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.techStack = techStack;
        this.description = description;
        this.githubLink = githubLink;
        this.demoLink = demoLink;
        this.result = result;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
