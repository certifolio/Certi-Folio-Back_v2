package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.ChatMessageRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.ChatMessageResponseDTO;
import com.certifolio.server.domain.mentoring.service.ChatService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ===== REST API (채팅방 관리) =====

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     * POST /api/chat/rooms
     */
    @PostMapping("/api/chat/rooms")
    public ApiResponse<ChatMessageResponseDTO.ChatRoomResponse> getOrCreateChattingRoom(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatMessageRequestDTO.CreateRoomRequest request) {

        ChatMessageResponseDTO.ChatRoomResponse room = chatService.getOrCreateChattingRoom(
                request.mentorId(), userId, request.menteeUserId());
        return ApiResponse.onSuccess("채팅방 조회 또는 생성 성공", room);
    }

    /**
     * 내 채팅방 목록 조회
     * GET /api/chat/rooms
     */
    @GetMapping("/api/chat/rooms")
    public ApiResponse<ChatMessageResponseDTO.ChatRoomListResponse> getMyChatRooms(
            @AuthenticationPrincipal Long userId) {

        ChatMessageResponseDTO.ChatRoomListResponse rooms = chatService.getMyChatRooms(userId);
        return ApiResponse.onSuccess("내 채팅방 목록 조회 성공", rooms);
    }

    // ===== WebSocket 메시지 처리 =====

    /**
     * 채팅 메시지 전송 (WebSocket STOMP)
     * 클라이언트 → /app/chat.send/{chatRoomId}
     * 서버 → /topic/chat.{chatRoomId} 로 브로드캐스트
     */
    @MessageMapping("/chat.send/{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId,
            @Payload ChatMessageRequestDTO.SendRequest request,
            Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        log.info("WebSocket message received: chatRoomId={}, userId={}", chatRoomId, userId);

        ChatMessageResponseDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, userId, request.content());

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);
    }

    /**
     * 채팅방 입장 알림 (WebSocket STOMP)
     * 클라이언트 → /app/chat.join/{chatRoomId}
     */
    @MessageMapping("/chat.join/{chatRoomId}")
    public void joinChat(@DestinationVariable Long chatRoomId,
            Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        ChatMessageResponseDTO.MessageResponse systemMsg = chatService.sendSystemMessage(
                chatRoomId, userId);

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, systemMsg);
    }

    // ===== REST API (채팅 기록) =====

    /**
     * REST로 메시지 전송 (WebSocket 없이도 사용 가능)
     * POST /api/chat/rooms/{chatRoomId}/send
     */
    @PostMapping("/api/chat/rooms/{chatRoomId}/send")
    public ApiResponse<ChatMessageResponseDTO.MessageResponse> sendMessageRest(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatMessageRequestDTO.SendRequest request) {

        ChatMessageResponseDTO.MessageResponse response = chatService.sendMessage(
                chatRoomId, userId, request.content());

        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, response);

        return ApiResponse.onSuccess("메시지 전송 성공", response);
    }

    /**
     * 커서 기반 메시지 조회
     * GET /api/chat/rooms/{chatRoomId}/messages?cursor={lastMessageId}&size={size}
     * cursor 없으면 최신 메시지부터, 있으면 해당 id 이전 메시지 조회
     */
    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public ApiResponse<ChatMessageResponseDTO.ChatHistoryResponse> getMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int size) {

        ChatMessageResponseDTO.ChatHistoryResponse history = chatService.getMessages(chatRoomId, userId, cursor, size);
        return ApiResponse.onSuccess("메시지 조회 성공", history);
    }

}
