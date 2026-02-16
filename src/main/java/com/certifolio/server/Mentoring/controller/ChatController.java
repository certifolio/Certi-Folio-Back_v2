package com.certifolio.server.Mentoring.controller;

import com.certifolio.server.Mentoring.dto.ChatMessageDTO;
import com.certifolio.server.Mentoring.service.ChatService;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.auth.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ===== WebSocket 메시지 처리 =====

    /**
     * 채팅 메시지 전송 (WebSocket STOMP)
     * 클라이언트 → /app/chat.send/{sessionId}
     * 서버 → /topic/chat.{sessionId} 로 브로드캐스트
     */
    @MessageMapping("/chat.send/{sessionId}")
    public void sendMessage(@DestinationVariable Long sessionId,
            @Payload ChatMessageDTO.SendRequest request) {
        log.info("WebSocket message received: sessionId={}, sender={}", sessionId, request.getSenderSubject());

        // subject에서 User 찾기
        User sender = resolveUserFromSubject(request.getSenderSubject());
        if (sender == null) {
            log.warn("Unable to resolve user from subject: {}", request.getSenderSubject());
            return;
        }

        // 메시지 저장 및 응답 생성
        ChatMessageDTO.MessageResponse response = chatService.sendMessage(
                sessionId, sender.getId(), request.getContent());

        // 해당 세션 구독자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat." + sessionId, response);
    }

    /**
     * 채팅방 입장 알림 (WebSocket STOMP)
     * 클라이언트 → /app/chat.join/{sessionId}
     */
    @MessageMapping("/chat.join/{sessionId}")
    public void joinChat(@DestinationVariable Long sessionId,
            @Payload ChatMessageDTO.SendRequest request) {
        User user = resolveUserFromSubject(request.getSenderSubject());
        if (user == null)
            return;

        String userName = user.getNickname() != null ? user.getNickname() : user.getName();

        ChatMessageDTO.MessageResponse systemMsg = chatService.sendSystemMessage(
                sessionId, user.getId(), userName + "님이 채팅에 참가했습니다.");

        messagingTemplate.convertAndSend("/topic/chat." + sessionId, systemMsg);
    }

    // ===== REST API (채팅 기록 조회용) =====

    /**
     * 채팅 기록 전체 조회
     * GET /api/mentoring/sessions/{sessionId}/chat
     */
    @GetMapping("/api/mentoring/sessions/{sessionId}/chat")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        Long userId = (user != null) ? user.getId() : null;

        ChatMessageDTO.ChatHistoryResponse history = chatService.getChatHistory(sessionId, userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 메시지 조회 (최대 50개)
     * GET /api/mentoring/sessions/{sessionId}/chat/recent
     */
    @GetMapping("/api/mentoring/sessions/{sessionId}/chat/recent")
    public ResponseEntity<?> getRecentMessages(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        Long userId = (user != null) ? user.getId() : null;

        ChatMessageDTO.ChatHistoryResponse history = chatService.getRecentMessages(sessionId, userId);
        return ResponseEntity.ok(history);
    }

    // ===== Helper =====

    /**
     * JWT subject (provider:providerId) 로 User 조회
     */
    private User resolveUserFromSubject(String subject) {
        if (subject == null || !subject.contains(":"))
            return null;
        String[] parts = subject.split(":", 2);
        return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
    }
}
