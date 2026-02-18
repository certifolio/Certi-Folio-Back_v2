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

    // ===== REST API (채팅방 관리) =====

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     * POST /api/chat/rooms
     * Body: { "mentorId": 1 }
     */
    @PostMapping("/api/chat/rooms")
    public ResponseEntity<?> getOrCreateRoom(
            @AuthenticationPrincipal Object principal,
            @RequestBody ChatMessageDTO.CreateRoomRequest request) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        if (user == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        ChatMessageDTO.ChatRoomResponse room = chatService.getOrCreateChatRoom(
                request.getMentorId(), user.getId());
        return ResponseEntity.ok(room);
    }

    /**
     * 내 채팅방 목록 조회
     * GET /api/chat/rooms
     */
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<?> getMyChatRooms(
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        if (user == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        ChatMessageDTO.ChatRoomListResponse rooms = chatService.getMyChatRooms(user.getId());
        return ResponseEntity.ok(rooms);
    }

    // ===== WebSocket 메시지 처리 =====

    /**
     * 채팅 메시지 전송 (WebSocket STOMP)
     * 클라이언트 → /app/chat.send/{chatRoomId}
     * 서버 → /topic/chat.{chatRoomId} 로 브로드캐스트
     */
    @MessageMapping("/chat.send/{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO.SendRequest request) {
        log.info("WebSocket message received: chatRoomId={}, sender={}", chatRoomId, request.getSenderSubject());

        // subject에서 User 찾기
        User sender = resolveUserFromSubject(request.getSenderSubject());
        if (sender == null) {
            log.warn("Unable to resolve user from subject: {}", request.getSenderSubject());
            return;
        }

        // 메시지 저장 및 응답 생성
        ChatMessageDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, sender.getId(), request.getContent());

        // 해당 채팅방 구독자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);
    }

    /**
     * 채팅방 입장 알림 (WebSocket STOMP)
     * 클라이언트 → /app/chat.join/{chatRoomId}
     */
    @MessageMapping("/chat.join/{chatRoomId}")
    public void joinChat(@DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO.SendRequest request) {
        User user = resolveUserFromSubject(request.getSenderSubject());
        if (user == null)
            return;

        String userName = user.getName();

        ChatMessageDTO.MessageResponse systemMsg = chatService.sendSystemMessage(
                chatRoomId, user.getId(), userName + "님이 채팅에 참가했습니다.");

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, systemMsg);
    }

    // ===== REST API (채팅 기록 조회용) =====

    /**
     * REST로 메시지 전송 (WebSocket 없이도 사용 가능)
     * POST /api/chat/rooms/{chatRoomId}/send
     */
    @PostMapping("/api/chat/rooms/{chatRoomId}/send")
    public ResponseEntity<?> sendMessageRest(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Object principal,
            @RequestBody ChatMessageDTO.SendRequest request) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        if (user == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        ChatMessageDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, user.getId(), request.getContent());

        // WebSocket 구독자에게도 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 기록 전체 조회
     * GET /api/chat/rooms/{chatRoomId}/messages
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        Long userId = (user != null) ? user.getId() : null;

        ChatMessageDTO.ChatHistoryResponse history = chatService.getChatHistory(chatRoomId, userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 메시지 조회 (최대 50개)
     * GET /api/chat/rooms/{chatRoomId}/messages/recent
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages/recent")
    public ResponseEntity<?> getRecentMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        Long userId = (user != null) ? user.getId() : null;

        ChatMessageDTO.ChatHistoryResponse history = chatService.getRecentMessages(chatRoomId, userId);
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
