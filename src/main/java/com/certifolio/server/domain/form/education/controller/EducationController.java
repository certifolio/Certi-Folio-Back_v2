package com.certifolio.server.domain.form.education.controller;

import com.certifolio.server.domain.form.education.dto.request.EducationRequestDTO;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.form.education.service.EducationService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/educations")
public class EducationController {

    private final EducationService educationService;

    // 학력 전체 조회
    @GetMapping
    public ApiResponse<List<EducationResponseDTO>> getEducations(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("학력 전체 조회 성공", educationService.getEducations(userId));
    }

    // 학력 단건 조회
    @GetMapping("/{educationId}")
    public ApiResponse<EducationResponseDTO> getEducation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long educationId
    ) {
        return ApiResponse.onSuccess("학력 조회 성공", educationService.getEducation(userId, educationId));
    }

    // 학력 전체 저장 (최초)
    @PostMapping
    public ApiResponse<Void> saveEducation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody List<EducationRequestDTO> request
    ) {
        educationService.saveEducation(userId, request);
        return ApiResponse.onSuccess("학력 저장 성공");
    }

    // 학력 단건 추가
    @PostMapping("/add")
    public ApiResponse<EducationResponseDTO> addEducation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody EducationRequestDTO request
    ) {
        return ApiResponse.onSuccess("학력 추가 성공", educationService.addEducation(userId, request));
    }

    // 학력 단건 수정
    @PatchMapping("/{educationId}")
    public ApiResponse<EducationResponseDTO> modifyEducation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequestDTO request
    ) {
        return ApiResponse.onSuccess("학력 수정 성공", educationService.modifyEducation(userId, educationId, request));
    }

    // 학력 단건 삭제
    @DeleteMapping("/{educationId}")
    public ApiResponse<Void> deleteEducation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long educationId
    ) {
        educationService.deleteEducation(userId, educationId);
        return ApiResponse.onSuccess("학력 삭제 성공");
    }
}
