package com.certifolio.server.Form.Education.controller;

import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.Education.service.EducationService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/educations")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> saveEducation(@AuthenticationPrincipal Object principal,
            @RequestBody EducationDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        EducationDTO saved = educationService.saveEducation(userId, dto);
        return ResponseEntity.ok(Map.of("success", true, "education", saved));
    }

    @GetMapping
    public ResponseEntity<?> getEducation(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        Object data = educationService.getEducation(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}