package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.MentoringApplicationRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentoringApplicationResponseDTO;
import com.certifolio.server.domain.mentoring.service.MentoringApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentoring-applications")
@RequiredArgsConstructor
@Slf4j
public class MentoringApplicationController {

    private final MentoringApplicationService applicationService;

    /**
     * 멘토링 신청
     * POST /api/mentoring-applications
     */
    @PostMapping
    public ResponseEntity<MentoringApplicationResponseDTO.CreateResponse> createApplication(
            @AuthenticationPrincipal Long userId,
            @RequestBody MentoringApplicationRequestDTO.CreateRequest request) {

        MentoringApplicationResponseDTO.CreateResponse response = applicationService.createApplication(userId, request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 받은 신청 목록 조회 (멘토용)
     * GET /api/mentoring-applications/received
     */
    @GetMapping("/received")
    public ResponseEntity<MentoringApplicationResponseDTO.ApplicationsResponse> getReceivedApplications(
            @AuthenticationPrincipal Long userId) {

        MentoringApplicationResponseDTO.ApplicationsResponse response = applicationService.getReceivedApplications(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 보낸 신청 목록 조회 (멘티용)
     * GET /api/mentoring-applications/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<MentoringApplicationResponseDTO.ApplicationsResponse> getSentApplications(
            @AuthenticationPrincipal Long userId) {

        MentoringApplicationResponseDTO.ApplicationsResponse response = applicationService.getSentApplications(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 신청 승인
     * POST /api/mentoring-applications/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<MentoringApplicationResponseDTO.ActionResponse> approveApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        MentoringApplicationResponseDTO.ActionResponse response = applicationService.approveApplication(userId, id);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 신청 거절
     * POST /api/mentoring-applications/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<MentoringApplicationResponseDTO.ActionResponse> rejectApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody(required = false) MentoringApplicationRequestDTO.RejectRequest request) {

        String reason = request != null ? request.reason() : null;
        MentoringApplicationResponseDTO.ActionResponse response = applicationService.rejectApplication(userId, id, reason);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
