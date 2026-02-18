package com.certifolio.server.Mentoring.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.MentoringApplicationDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Mentoring.service.MentoringApplicationService;
import com.certifolio.server.auth.util.AuthUtils;
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
    private final UserRepository userRepository;

    /**
     * 멘토링 신청
     * POST /api/mentoring-applications
     */
    @PostMapping
    public ResponseEntity<MentoringApplicationDTO.CreateResponse> createApplication(
            @AuthenticationPrincipal Object principal,
            @RequestBody MentoringApplicationDTO.CreateRequest request) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentoringApplicationDTO.CreateResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        MentoringApplicationDTO.CreateResponse response = applicationService.createApplication(user.getId(), request);

        if (response.isSuccess()) {
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
    public ResponseEntity<MentoringApplicationDTO.ApplicationsResponse> getReceivedApplications(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MentoringApplicationDTO.ApplicationsResponse response = applicationService
                .getReceivedApplications(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 보낸 신청 목록 조회 (멘티용)
     * GET /api/mentoring-applications/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<MentoringApplicationDTO.ApplicationsResponse> getSentApplications(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MentoringApplicationDTO.ApplicationsResponse response = applicationService.getSentApplications(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 신청 승인
     * POST /api/mentoring-applications/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<MentoringApplicationDTO.ActionResponse> approveApplication(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentoringApplicationDTO.ActionResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        MentoringApplicationDTO.ActionResponse response = applicationService.approveApplication(user.getId(), id);

        if (response.isSuccess()) {
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
    public ResponseEntity<MentoringApplicationDTO.ActionResponse> rejectApplication(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long id,
            @RequestBody(required = false) MentoringApplicationDTO.RejectRequest request) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentoringApplicationDTO.ActionResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        String reason = request != null ? request.getReason() : null;
        MentoringApplicationDTO.ActionResponse response = applicationService.rejectApplication(user.getId(), id,
                reason);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Principal에서 User 조회
     */
    private User getUser(Object principal) {
        return AuthUtils.resolveUser(principal, userRepository);
    }
}
