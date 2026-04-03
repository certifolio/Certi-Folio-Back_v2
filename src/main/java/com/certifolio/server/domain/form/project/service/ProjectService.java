package com.certifolio.server.domain.form.project.service;

import com.certifolio.server.domain.form.project.dto.request.ProjectRequestDTO;
import com.certifolio.server.domain.form.project.dto.response.ProjectResponseDTO;
import com.certifolio.server.domain.form.project.entity.Project;
import com.certifolio.server.domain.form.project.repository.ProjectRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserService userService;
    private final ProjectRepository projectRepository;

    // 프로젝트 전체 조회
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjects(Long userId) {
        return projectRepository.findAllByUserId(userId).stream()
                .map(ProjectResponseDTO::from)
                .toList();
    }

    // 프로젝트 전체 저장 (최초)
    @Transactional
    public void saveProject(Long userId, List<ProjectRequestDTO> request) {
        User user = userService.getUserById(userId);

        if (request == null || request.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.PROJECT_NOT_INPUTTED);
        }

        List<Project> projects = request.stream()
                .map(dto -> Project.builder()
                        .user(user)
                        .name(dto.name())
                        .type(dto.type())
                        .role(dto.role())
                        .techStack(dto.techStack())
                        .description(dto.description())
                        .githubLink(dto.githubLink())
                        .demoLink(dto.demoLink())
                        .result(dto.result())
                        .startDate(DateUtils.parseDate(dto.startDate()))
                        .endDate(DateUtils.parseDate(dto.endDate()))
                        .build())
                .toList();

        projectRepository.saveAll(projects);
    }

    // 프로젝트 단건 조회
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProject(Long userId, Long projectId) {
        Project project = getProjectWithOwnerCheck(userId, projectId);
        return ProjectResponseDTO.from(project);
    }

    // 프로젝트 단건 추가
    @Transactional
    public ProjectResponseDTO addProject(Long userId, ProjectRequestDTO request) {
        User user = userService.getUserById(userId);
        Project project = Project.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .role(request.role())
                .techStack(request.techStack())
                .description(request.description())
                .githubLink(request.githubLink())
                .demoLink(request.demoLink())
                .result(request.result())
                .startDate(DateUtils.parseDate(request.startDate()))
                .endDate(DateUtils.parseDate(request.endDate()))
                .build();
        return ProjectResponseDTO.from(projectRepository.save(project));
    }

    // 프로젝트 단건 수정
    @Transactional
    public ProjectResponseDTO modifyProject(Long userId, Long projectId, ProjectRequestDTO request) {
        Project project = getProjectWithOwnerCheck(userId, projectId);
        project.update(
                request.name(),
                request.type(),
                request.role(),
                request.techStack(),
                request.description(),
                request.githubLink(),
                request.demoLink(),
                request.result(),
                DateUtils.parseDate(request.startDate()),
                DateUtils.parseDate(request.endDate())
        );
        return ProjectResponseDTO.from(project);
    }

    // 프로젝트 단건 삭제
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        Project project = getProjectWithOwnerCheck(userId, projectId);
        projectRepository.delete(project);
    }

    // 프로젝트 확인
    private Project getProjectWithOwnerCheck(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.PROJECT_NOT_FOUND));
        if (!project.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.PROJECT_UNAUTHORIZED);
        }
        return project;
    }
}
