package com.certifolio.server.domain.form.career.controller;

import com.certifolio.server.domain.form.career.dto.request.CareerRequestDTO;
import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.career.service.CareerService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/careers")
public class CareerController {

    private final CareerService careerService;

    // 경력 전체 조회
    @GetMapping
    public ApiResponse<List<CareerResponseDTO>> getCareers(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("경력 사항 전체 조회 성공", careerService.getCareers(userId));
    }

    // 경력 단건 조회
    @GetMapping("/{careerId}")
    public ApiResponse<CareerResponseDTO> getCareer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long careerId
    ) {
        return ApiResponse.onSuccess("경력 사항 조회 성공", careerService.getCareer(userId, careerId));
    }

    // 경력 전체 저장 (최초)
    @PostMapping
    public ApiResponse<Void> saveCareer(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody List<CareerRequestDTO> request
    ) {
        careerService.saveCareer(userId, request);
        return ApiResponse.onSuccess("경력 사항 저장 성공");
    }

    // 경력 단건 추가
    @PostMapping("/add")
    public ApiResponse<CareerResponseDTO> addCareer(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CareerRequestDTO request
    ) {
        return ApiResponse.onSuccess("경력 사항 추가 성공", careerService.addCareer(userId, request));
    }

    // 경력 단건 수정
    @PatchMapping("/{careerId}")
    public ApiResponse<CareerResponseDTO> modifyCareer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long careerId,
            @Valid @RequestBody CareerRequestDTO request
    ) {
        return ApiResponse.onSuccess("경력 사항 수정 성공", careerService.modifyCareer(userId, careerId, request));
    }

    // 경력 단건 삭제
    @DeleteMapping("/{careerId}")
    public ApiResponse<Void> deleteCareer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long careerId
    ) {
        careerService.deleteCareer(userId, careerId);
        return ApiResponse.onSuccess("경력 사항 삭제 성공");
    }
}
