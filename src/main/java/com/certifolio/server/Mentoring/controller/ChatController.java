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

        try {
            // 멘토가 채팅방을 만들 때: request에 userId가 있으면 해당 멘티의 ID 사용
            Long targetUserId = user.getId();
            if (request.getUserId() != null) {
                targetUserId = request.getUserId();
            }

            ChatMessageDTO.ChatRoomResponse room = chatService.getOrCreateChatRoom(
                    request.getMentorId(), targetUserId);
            return ResponseEntity.ok(room);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(
                    java.util.Map.of("success", false, "message", e.getMessage()));
        }
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
     *
     * 응답에 sequenceNumber가 포함되어 클라이언트에서 순서 정렬 가능
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

        // 메시지 저장 및 응답 생성 (시퀀스 번호 자동 부여됨)
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

    /**
     * 메시지 수신 확인 (ACK) 처리 (WebSocket STOMP)
     * 클라이언트 → /app/chat.ack/{chatRoomId}
     * 서버 → /queue/ack 으로 ACK 결과 응답 (개인 메시지)
     *
     * 클라이언트는 메시지를 수신하면 이 핸들러로 ACK를 보내야 합니다.
     * ACK가 오지 않으면 서버가 주기적으로 재전송합니다.
     */
    @MessageMapping("/chat.ack/{chatRoomId}")
    public void acknowledgeMessage(@DestinationVariable Long chatRoomId,
            @Payload ChatMessageDTO.AckRequest request) {
        log.info("ACK received: chatRoomId={}, messageId={}", chatRoomId, request.getMessageId());

        try {
            ChatMessageDTO.AckResponse ackResponse = chatService.acknowledgeMessage(request.getMessageId());

            // ACK 전송자에게 확인 응답 (개인 큐)
            if (request.getSenderSubject() != null) {
                messagingTemplate.convertAndSend(
                        "/queue/ack." + request.getSenderSubject(), ackResponse);
            }
        } catch (Exception e) {
            log.error("ACK processing failed: messageId={}, error={}", request.getMessageId(), e.getMessage());
        }
    }

    // ===== REST API (채팅 기록 / 동기화) =====

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
     * 채팅 기록 전체 조회 (시퀀스 번호 순서대로 정렬)
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

    /**
     * 재접속 시 누락 메시지 동기화 API
     * GET /api/chat/rooms/{chatRoomId}/sync?lastSeq=N
     *
     * 클라이언트가 마지막으로 수신한 시퀀스 번호를 전달하면,
     * 그 이후의 누락된 메시지를 시퀀스 순서대로 반환합니다.
     * 네트워크 단절 후 재접속 시 이전 대화를 완벽히 복구할 수 있습니다.
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/sync")
    public ResponseEntity<?> syncMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(name = "lastSeq", defaultValue = "0") Long lastSequenceNumber,
            @AuthenticationPrincipal Object principal) {

        User user = AuthUtils.resolveUser(principal, userRepository);
        Long userId = (user != null) ? user.getId() : null;

        ChatMessageDTO.ChatHistoryResponse missed = chatService.getMissedMessages(
                chatRoomId, lastSequenceNumber, userId);
        return ResponseEntity.ok(missed);
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
