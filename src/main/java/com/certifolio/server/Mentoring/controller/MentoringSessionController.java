package com.certifolio.server.Mentoring.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.MentoringSessionDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Mentoring.service.MentoringSessionService;
import com.certifolio.server.auth.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentoring/sessions")
@RequiredArgsConstructor
@Slf4j
public class MentoringSessionController {

    private final MentoringSessionService sessionService;
    private final UserRepository userRepository;

    /**
     * 내 멘토링 세션 목록 조회
     * GET /api/mentoring/sessions
     */
    @GetMapping
    public ResponseEntity<MentoringSessionDTO.SessionsResponse> getMySessions(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MentoringSessionDTO.SessionsResponse response = sessionService.getMySessions(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 세션 상세 조회
     * GET /api/mentoring/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<MentoringSessionDTO.SessionItem> getSession(
            @PathVariable Long sessionId) {

        MentoringSessionDTO.SessionItem response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 새 세션 생성
     * POST /api/mentoring/sessions
     */
    @PostMapping
    public ResponseEntity<MentoringSessionDTO.UpdateSessionResponse> createSession(
            @AuthenticationPrincipal Object principal,
            @RequestBody MentoringSessionDTO.CreateSessionRequest request) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentoringSessionDTO.UpdateSessionResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        MentoringSessionDTO.UpdateSessionResponse response = sessionService.createSession(user.getId(), request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 세션 상태 업데이트
     * PATCH /api/mentoring/sessions/{sessionId}/status
     */
    @PatchMapping("/{sessionId}/status")
    public ResponseEntity<MentoringSessionDTO.UpdateSessionResponse> updateSessionStatus(
            @PathVariable Long sessionId,
            @RequestBody MentoringSessionDTO.UpdateSessionStatusRequest request) {

        MentoringSessionDTO.UpdateSessionResponse response = sessionService.updateSessionStatus(sessionId, request);

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
