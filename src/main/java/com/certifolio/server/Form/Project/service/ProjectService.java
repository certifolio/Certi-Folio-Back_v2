package com.certifolio.server.Form.Project.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Project.domain.Project;
import com.certifolio.server.Form.Project.dto.ProjectDTO;
import com.certifolio.server.Form.util.DateUtils;
import com.certifolio.server.Form.Project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 프로젝트 전체 저장 (기존 삭제 후 재저장)
     */
    public void saveProjects(Long userId, List<ProjectDTO> dtos) {
        User user = getUser(userId);

        // 기존 데이터 삭제
        projectRepository.findAllByUser(user).forEach(projectRepository::delete);

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<Project> projects = dtos.stream()
                .map(dto -> Project.builder()
                        .user(user)
                        .name(dto.getName())
                        .type(dto.getType())
                        .role(dto.getRole())
                        .techStack(dto.getTechStack())
                        .description(dto.getDescription())
                        .githubLink(dto.getGithubLink())
                        .demoLink(dto.getDemoLink())
                        .result(dto.getResult())
                        .startDate(DateUtils.parseDate(dto.getStartDate()))
                        .endDate(DateUtils.parseDate(dto.getEndDate()))
                        .build())
                .collect(Collectors.toList());

        projectRepository.saveAll(projects);
    }

    /**
     * 프로젝트 목록 조회
     */
    public List<ProjectDTO> getProjects(Long userId) {
        User user = getUser(userId);
        return projectRepository.findAllByUser(user).stream()
                .map(ProjectDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 삭제
     */
    public void deleteProject(Long userId, Long projectId) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!p.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        projectRepository.delete(p);
    }



    public ProjectDTO addProject(Long userId, ProjectDTO dto) {
        User user = getUser(userId);
        Project project = Project.builder()
                .user(user)
                .name(dto.getName())
                .type(dto.getType())
                .role(dto.getRole())
                .techStack(dto.getTechStack())
                .description(dto.getDescription())
                .githubLink(dto.getGithubLink())
                .demoLink(dto.getDemoLink())
                .result(dto.getResult())
                .startDate(DateUtils.parseDate(dto.getStartDate()))
                .endDate(DateUtils.parseDate(dto.getEndDate()))
                .build();

        Project saved = projectRepository.save(project);

        return ProjectDTO.from(saved);
    }

    public ProjectDTO updateProject(Long userId, Long id, ProjectDTO dto) {
        User user = getUser(userId);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.update(dto.getName(), dto.getType(), dto.getRole(), dto.getTechStack(), dto.getDescription(), dto.getGithubLink(), dto.getDemoLink(), dto.getResult(), DateUtils.parseDate(dto.getStartDate()), DateUtils.parseDate(dto.getEndDate()));

        Project saved = projectRepository.save(project);

        return ProjectDTO.from(saved);
    }
}
