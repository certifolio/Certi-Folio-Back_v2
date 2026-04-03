package com.certifolio.server.domain.form.certificate.controller;

import com.certifolio.server.domain.form.certificate.dto.request.CertificateRequestDTO;
import com.certifolio.server.domain.form.certificate.dto.response.CertificateResponseDTO;
import com.certifolio.server.domain.form.certificate.service.CertificateService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specs/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    // 자격증 전체 조회
    @GetMapping
    public ApiResponse<List<CertificateResponseDTO>> getCertificates(@AuthenticationPrincipal Long userId) {
        return ApiResponse.onSuccess("자격증 전체 조회 성공", certificateService.getCertificates(userId));
    }

    // 자격증 단건 조회
    @GetMapping("/{certificateId}")
    public ApiResponse<CertificateResponseDTO> getCertificate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long certificateId
    ) {
        return ApiResponse.onSuccess("자격증 조회 성공", certificateService.getCertificate(userId, certificateId));
    }

    // 자격증 전체 저장 (최초)
    @PostMapping
    public ApiResponse<Void> saveCertificate(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody List<CertificateRequestDTO> request
    ) {
        certificateService.saveCertificate(userId, request);
        return ApiResponse.onSuccess("자격증 저장 성공");
    }

    // 자격증 단건 추가
    @PostMapping("/add")
    public ApiResponse<CertificateResponseDTO> addCertificate(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CertificateRequestDTO request
    ) {
        return ApiResponse.onSuccess("자격증 추가 성공", certificateService.addCertificate(userId, request));
    }

    // 자격증 단건 수정
    @PatchMapping("/{certificateId}")
    public ApiResponse<CertificateResponseDTO> modifyCertificate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long certificateId,
            @Valid @RequestBody CertificateRequestDTO request
    ) {
        return ApiResponse.onSuccess("자격증 수정 성공", certificateService.modifyCertificate(userId, certificateId, request));
    }

    // 자격증 단건 삭제
    @DeleteMapping("/{certificateId}")
    public ApiResponse<Void> deleteCertificate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long certificateId
    ) {
        certificateService.deleteCertificate(userId, certificateId);
        return ApiResponse.onSuccess("자격증 삭제 성공");
    }
}
