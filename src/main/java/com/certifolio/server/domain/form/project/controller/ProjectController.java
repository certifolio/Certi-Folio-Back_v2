package com.certifolio.server.domain.form.project.controller;

import com.certifolio.server.domain.form.project.dto.request.ProjectRequestDTO;
import com.certifolio.server.domain.form.project.dto.response.ProjectResponseDTO;
import com.certifolio.server.domain.form.project.service.ProjectService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/projects")
public class ProjectController {

    private final ProjectService projectService;

    // 프로젝트 전체 조회
    @GetMapping
    public ApiResponse<List<ProjectResponseDTO>> getProjects(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("프로젝트 전체 조회 성공", projectService.getProjects(userId));
    }

    // 프로젝트 단건 조회
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponseDTO> getProject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId
    ) {
        return ApiResponse.onSuccess("프로젝트 조회 성공", projectService.getProject(userId, projectId));
    }

    // 프로젝트 전체 저장 (최초)
    @PostMapping
    public ApiResponse<Void> saveProject(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody List<ProjectRequestDTO> request
    ) {
        projectService.saveProject(userId, request);
        return ApiResponse.onSuccess("프로젝트 저장 성공");
    }

    // 프로젝트 단건 추가
    @PostMapping("/add")
    public ApiResponse<ProjectResponseDTO> addProject(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProjectRequestDTO request
    ) {
        return ApiResponse.onSuccess("프로젝트 추가 성공", projectService.addProject(userId, request));
    }

    // 프로젝트 단건 수정
    @PatchMapping("/{projectId}")
    public ApiResponse<ProjectResponseDTO> modifyProject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDTO request
    ) {
        return ApiResponse.onSuccess("프로젝트 수정 성공", projectService.modifyProject(userId, projectId, request));
    }

    // 프로젝트 단건 삭제
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId
    ) {
        projectService.deleteProject(userId, projectId);
        return ApiResponse.onSuccess("프로젝트 삭제 성공");
    }
}
