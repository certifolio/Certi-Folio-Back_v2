package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.MentoringSessionRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentoringSessionResponseDTO;
import com.certifolio.server.domain.mentoring.service.MentoringSessionService;
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

    /**
     * 내 멘토링 세션 목록 조회
     * GET /api/mentoring/sessions
     */
    @GetMapping
    public ResponseEntity<MentoringSessionResponseDTO.SessionsResponse> getMySessions(
            @AuthenticationPrincipal Long userId) {

        MentoringSessionResponseDTO.SessionsResponse response = sessionService.getMySessions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 세션 상세 조회
     * GET /api/mentoring/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<MentoringSessionResponseDTO.SessionItem> getSession(
            @PathVariable Long sessionId) {

        MentoringSessionResponseDTO.SessionItem response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 새 세션 생성
     * POST /api/mentoring/sessions
     */
    @PostMapping
    public ResponseEntity<MentoringSessionResponseDTO.UpdateSessionResponse> createSession(
            @AuthenticationPrincipal Long userId,
            @RequestBody MentoringSessionRequestDTO.CreateSessionRequest request) {

        MentoringSessionResponseDTO.UpdateSessionResponse response = sessionService.createSession(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 세션 상태 업데이트
     * PATCH /api/mentoring/sessions/{sessionId}/status
     */
    @PatchMapping("/{sessionId}/status")
    public ResponseEntity<MentoringSessionResponseDTO.UpdateSessionResponse> updateSessionStatus(
            @PathVariable Long sessionId,
            @RequestBody MentoringSessionRequestDTO.UpdateSessionStatusRequest request) {

        MentoringSessionResponseDTO.UpdateSessionResponse response = sessionService.updateSessionStatus(sessionId, request);
        return ResponseEntity.ok(response);
    }
}
