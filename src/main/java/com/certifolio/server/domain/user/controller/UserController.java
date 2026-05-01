package com.certifolio.server.domain.user.controller;

import com.certifolio.server.domain.user.dto.request.UserRequestDTO;
import com.certifolio.server.domain.user.dto.response.UserResponseDTO;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserResponseDTO> getMyInfo(@AuthenticationPrincipal Long userId) {
        User user = userService.getUserById(userId);
        return ApiResponse.onSuccess("내 정보 조회 성공", UserResponseDTO.from(user));
    }

    // 온보딩 저장
    @PostMapping("/me/onboarding")
    public ApiResponse<Void> saveOnboarding(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO request
    ) {
        User user = userService.getUserById(userId);
        userService.saveOnBoarding(user, request.name(), request.companyType(), request.jobRole());
        return ApiResponse.onSuccess("온보딩 정보 저장 성공");
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ApiResponse<Void> updateMyInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO request
    ) {
        User user = userService.getUserById(userId);
        userService.saveOnBoarding(user, request.name(), request.companyType(), request.jobRole());
        return ApiResponse.onSuccess("내 정보 수정 성공");
    }

    @PostMapping("/me/profile-image")
    public ApiResponse<String> uploadProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        String imageUrl = userService.updateProfileImage(userId, file);
        return ApiResponse.onSuccess("프로필 이미지 업로드 성공", imageUrl);
    }
}
