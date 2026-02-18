package com.certifolio.server.Form.Project.controller;

import com.certifolio.server.Form.Project.dto.ProjectDTO;
import com.certifolio.server.Form.Project.service.ProjectService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> saveProjects(@AuthenticationPrincipal Object principal,
                                          @RequestBody List<ProjectDTO> dtos) {
        Long userId = authenticationHelper.getUserId(principal);
        projectService.saveProjects(userId, dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping
    public ResponseEntity<?> getProjects(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        return ResponseEntity.ok(Map.of("success", true, "data", projectService.getProjects(userId)));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProject(@AuthenticationPrincipal Object principal,
                                        @RequestBody ProjectDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        ProjectDTO saved = projectService.addProject(userId, dto);
        return ResponseEntity.ok(Map.of("success", true, "project", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        Long userId = authenticationHelper.getUserId(principal);
        projectService.deleteProject(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@AuthenticationPrincipal Object principal,
                                           @PathVariable Long id,
                                           @RequestBody ProjectDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        ProjectDTO updated = projectService.updateProject(userId, id, dto);
        return ResponseEntity.ok(Map.of("success", true, "project", updated));
    }
}
