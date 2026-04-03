package com.certifolio.server.domain.form.activity.controller;

import com.certifolio.server.domain.form.activity.dto.request.ActivityRequestDTO;
import com.certifolio.server.domain.form.activity.dto.response.ActivityResponseDTO;
import com.certifolio.server.domain.form.activity.service.ActivityService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/activities")
public class ActivityController {

    private final ActivityService activityService;

    // 활동 전체 조회
    @GetMapping
    public ApiResponse<List<ActivityResponseDTO>> getActivities(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("활동 내역 전체 조회 성공", activityService.getActivities(userId));
    }

    // 활동 단건 조회
    @GetMapping("/{activityId}")
    public ApiResponse<ActivityResponseDTO> getActivity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long activityId
    ) {
        return ApiResponse.onSuccess("활동 내역 조회 성공", activityService.getActivity(userId, activityId));
    }

    // 활동 전체 저장 (최초)
    @PostMapping
    public ApiResponse<Void> saveActivity(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody List<ActivityRequestDTO> request
    ) {
        activityService.saveActivity(userId, request);
        return ApiResponse.onSuccess("활동 내역 저장 성공");
    }

    // 활동 단건 추가
    @PostMapping("/add")
    public ApiResponse<ActivityResponseDTO> addActivity(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ActivityRequestDTO request
    ) {
        return ApiResponse.onSuccess("활동 내역 추가 성공", activityService.addActivity(userId, request));
    }

    // 활동 단건 수정
    @PatchMapping("/{activityId}")
    public ApiResponse<ActivityResponseDTO> modifyActivity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityRequestDTO request
    ) {
        return ApiResponse.onSuccess("활동 내역 수정 성공", activityService.modifyActivity(userId, activityId, request));
    }

    // 활동 단건 삭제
    @DeleteMapping("/{activityId}")
    public ApiResponse<Void> deleteActivity(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long activityId
    ) {
        activityService.deleteActivity(userId, activityId);
        return ApiResponse.onSuccess("활동 내역 삭제 성공");
    }
}
