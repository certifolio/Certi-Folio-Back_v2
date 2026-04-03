package com.certifolio.server.domain.form.algorithm.controller;

import com.certifolio.server.domain.form.algorithm.dto.request.AlgorithmRequestDTO;
import com.certifolio.server.domain.form.algorithm.dto.response.AlgorithmResponseDTO;
import com.certifolio.server.domain.form.algorithm.service.AlgorithmService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/algorithm")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    // 알고리즘 정보 조회
    @GetMapping
    public ApiResponse<AlgorithmResponseDTO> getAlgorithm(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("알고리즘 정보 조회 성공", algorithmService.getAlgorithm(userId));
    }

    // 핸들 등록 및 데이터 저장
    @PostMapping
    public ApiResponse<AlgorithmResponseDTO> saveAlgorithm(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AlgorithmRequestDTO request
    ) {
        return ApiResponse.onSuccess("알고리즘 정보 저장 성공", algorithmService.saveAlgorithm(userId, request));
    }

    // 데이터 갱신 (핸들 유지, solved.ac 재조회)
    @PatchMapping("/sync")
    public ApiResponse<AlgorithmResponseDTO> syncAlgorithm(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("알고리즘 정보 갱신 성공", algorithmService.syncAlgorithm(userId));
    }
}
