package com.certifolio.server.domain.portfolio.controller;

import com.certifolio.server.domain.portfolio.dto.request.PortfolioDraftUpdateRequest;
import com.certifolio.server.domain.portfolio.dto.response.PortfolioDraftResponse;
import com.certifolio.server.domain.portfolio.service.PortfolioDraftService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/draft")
public class PortfolioDraftController {

    private final PortfolioDraftService portfolioDraftService;

    @PostMapping("/generate")
    public ApiResponse<PortfolioDraftResponse> generate(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess("포트폴리오 초안 생성 성공", portfolioDraftService.generate(userId));
    }

    @GetMapping("/latest")
    public ApiResponse<PortfolioDraftResponse> getLatestDraft(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess("포트폴리오 초안 조회 성공", portfolioDraftService.getLatest(userId));
    }

    @PatchMapping("/{id}")
    public ApiResponse<PortfolioDraftResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody PortfolioDraftUpdateRequest request
    ) {
        return ApiResponse.onSuccess("포트폴리오 초안 수정 성공", portfolioDraftService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        portfolioDraftService.delete(userId, id);
        return ApiResponse.onSuccess("포트폴리오 초안 삭제 성공", null);
    }
}
