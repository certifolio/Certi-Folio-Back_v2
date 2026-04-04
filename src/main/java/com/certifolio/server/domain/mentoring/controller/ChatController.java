package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.ChatMessageRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.ChatMessageResponseDTO;
import com.certifolio.server.domain.mentoring.service.ChatService;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.repository.MentorRepository;
import com.certifolio.server.domain.user.repository.UserRepository;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
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
    private final MentorRepository mentorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ===== REST API (채팅방 관리) =====

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     * POST /api/chat/rooms
     */
    @PostMapping("/api/chat/rooms")
    public ResponseEntity<ChatMessageResponseDTO.ChatRoomResponse> getOrCreateRoom(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatMessageRequestDTO.CreateRoomRequest request) {

        Long targetUserId = userId;
        if (request.userId() != null) {
            // 멘토가 멘티를 지정하는 경우: 요청자가 해당 멘토의 소유자인지 검증
            Mentor mentor = mentorRepository.findById(request.mentorId())
                    .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));
            if (!mentor.getUser().getId().equals(userId)) {
                throw new BusinessException(GeneralErrorCode.CHAT_ROOM_CREATION_FORBIDDEN);
            }
            targetUserId = request.userId();
        }

        ChatMessageResponseDTO.ChatRoomResponse room = chatService.getOrCreateChatRoom(
                request.mentorId(), targetUserId);
        return ResponseEntity.ok(room);
    }

    /**
     * 내 채팅방 목록 조회
     * GET /api/chat/rooms
     */
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<?> getMyChatRooms(
            @AuthenticationPrincipal Long userId) {

        ChatMessageResponseDTO.ChatRoomListResponse rooms = chatService.getMyChatRooms(userId);
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
            @Payload ChatMessageRequestDTO.SendRequest request) {
        log.info("WebSocket message received: chatRoomId={}, sender={}", chatRoomId, request.senderSubject());

        // subject(provider:providerId)에서 User 찾기
        User sender = resolveUserFromSubject(request.senderSubject());
        if (sender == null) {
            log.warn("Unable to resolve user from subject: {}", request.senderSubject());
            return;
        }

        ChatMessageResponseDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, sender.getId(), request.content());

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);
    }

    /**
     * 채팅방 입장 알림 (WebSocket STOMP)
     * 클라이언트 → /app/chat.join/{chatRoomId}
     */
    @MessageMapping("/chat.join/{chatRoomId}")
    public void joinChat(@DestinationVariable Long chatRoomId,
            @Payload ChatMessageRequestDTO.SendRequest request) {
        User user = resolveUserFromSubject(request.senderSubject());
        if (user == null)
            return;

        ChatMessageResponseDTO.MessageResponse systemMsg = chatService.sendSystemMessage(
                chatRoomId, user.getId(), user.getName() + "님이 채팅에 참가했습니다.");

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, systemMsg);
    }

    // ===== REST API (채팅 기록) =====

    /**
     * REST로 메시지 전송 (WebSocket 없이도 사용 가능)
     * POST /api/chat/rooms/{chatRoomId}/send
     */
    @PostMapping("/api/chat/rooms/{chatRoomId}/send")
    public ResponseEntity<?> sendMessageRest(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatMessageRequestDTO.SendRequest request) {

        ChatMessageResponseDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, userId, request.content());

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅 기록 전체 조회 (시간순 정렬)
     * GET /api/chat/rooms/{chatRoomId}/messages
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Long userId) {

        ChatMessageResponseDTO.ChatHistoryResponse history = chatService.getChatHistory(chatRoomId, userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 메시지 조회 (최대 50개)
     * GET /api/chat/rooms/{chatRoomId}/messages/recent
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages/recent")
    public ResponseEntity<?> getRecentMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Long userId) {

        ChatMessageResponseDTO.ChatHistoryResponse history = chatService.getRecentMessages(chatRoomId, userId);
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
