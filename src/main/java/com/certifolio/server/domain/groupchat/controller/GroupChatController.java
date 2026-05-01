package com.certifolio.server.domain.groupchat.controller;

import com.certifolio.server.domain.groupchat.dto.request.GroupChatRequestDTO;
import com.certifolio.server.domain.groupchat.dto.response.GroupChatResponseDTO;
import com.certifolio.server.domain.groupchat.service.GroupChatService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * 유저 채팅방 컨트롤러
 * - REST: 방 생성/조회/참여/나가기/메시지 조회
 * - WebSocket: 메시지 전송, presence 알림 (/topic/group-chat.{roomId})
 */
@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ===== REST API =====

    /**
     * 유저 채팅방 생성 (생성자 자동 참여, 1인당 최대 3개)
     * POST /api/group-chat/rooms
     */
    @PostMapping("/api/group-chat/rooms")
    public ApiResponse<GroupChatResponseDTO.RoomResponse> createRoom(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GroupChatRequestDTO.CreateRoomRequest request) {

        GroupChatResponseDTO.RoomResponse room = groupChatService.createRoom(userId, request);
        return ApiResponse.onSuccess("유저 채팅방 생성 성공", room);
    }

    /**
     * 전체 유저 채팅방 목록 조회
     * GET /api/group-chat/rooms
     */
    @GetMapping("/api/group-chat/rooms")
    public ApiResponse<GroupChatResponseDTO.RoomListResponse> getAllRooms(
            @AuthenticationPrincipal Long userId) {

        GroupChatResponseDTO.RoomListResponse rooms = groupChatService.getAllRooms(userId);
        return ApiResponse.onSuccess("유저 채팅방 목록 조회 성공", rooms);
    }

    /**
     * 내가 참여 중인 채팅방 목록
     * GET /api/group-chat/rooms/my
     */
    @GetMapping("/api/group-chat/rooms/my")
    public ApiResponse<GroupChatResponseDTO.RoomListResponse> getMyRooms(
            @AuthenticationPrincipal Long userId) {

        GroupChatResponseDTO.RoomListResponse rooms = groupChatService.getMyRooms(userId);
        return ApiResponse.onSuccess("내 유저 채팅방 목록 조회 성공", rooms);
    }

    /**
     * 채팅방 참여하기 - 시스템 메시지를 함께 broadcast
     * POST /api/group-chat/rooms/{roomId}/join
     */
    @PostMapping("/api/group-chat/rooms/{roomId}/join")
    public ApiResponse<GroupChatResponseDTO.JoinResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId) {

        GroupChatResponseDTO.JoinResult result = groupChatService.joinRoom(roomId, userId);
        messagingTemplate.convertAndSend("/topic/group-chat." + roomId, result.systemMessage());
        return ApiResponse.onSuccess("유저 채팅방 참여 성공", result.joinResponse());
    }

    /**
     * 채팅방 나가기
     * DELETE /api/group-chat/rooms/{roomId}/leave
     */
    @DeleteMapping("/api/group-chat/rooms/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId) {

        groupChatService.leaveRoom(roomId, userId);
        return ApiResponse.onSuccess("유저 채팅방 나가기 성공");
    }

    /**
     * 메시지 조회 (커서 기반 페이지네이션)
     * GET /api/group-chat/rooms/{roomId}/messages?cursor=&size=
     */
    @GetMapping("/api/group-chat/rooms/{roomId}/messages")
    public ApiResponse<GroupChatResponseDTO.MessageHistoryResponse> getMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {

        GroupChatResponseDTO.MessageHistoryResponse history =
                groupChatService.getMessages(roomId, userId, cursor, size);
        return ApiResponse.onSuccess("유저 채팅방 메시지 조회 성공", history);
    }

    /**
     * REST로 메시지 전송 (WebSocket 미사용 환경 대비)
     * POST /api/group-chat/rooms/{roomId}/send
     */
    @PostMapping("/api/group-chat/rooms/{roomId}/send")
    public ApiResponse<GroupChatResponseDTO.MessageResponse> sendMessageRest(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GroupChatRequestDTO.SendRequest request) {

        GroupChatResponseDTO.MessageResponse response =
                groupChatService.sendMessage(roomId, userId, request.content());

        messagingTemplate.convertAndSend("/topic/group-chat." + roomId, response);
        return ApiResponse.onSuccess("유저 채팅방 메시지 전송 성공", response);
    }

    // ===== WebSocket =====

    /**
     * WebSocket 메시지 전송
     * 클라이언트 → /app/group-chat.send/{roomId}
     * 서버 → /topic/group-chat.{roomId}
     */
    @MessageMapping("/group-chat.send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Valid @Payload GroupChatRequestDTO.SendRequest request,
                            Principal principal) {
        Long userId = requirePrincipalUserId(principal);
        log.info("유저 채팅방 WebSocket 메시지: roomId={}, userId={}", roomId, userId);

        GroupChatResponseDTO.MessageResponse response =
                groupChatService.sendMessage(roomId, userId, request.content());

        messagingTemplate.convertAndSend("/topic/group-chat." + roomId, response);
    }

    /**
     * WebSocket presence 알림 (재접속/입장 시) - DB 저장 없이 broadcast 전용
     * 클라이언트 → /app/group-chat.join/{roomId}
     */
    @MessageMapping("/group-chat.join/{roomId}")
    public void joinChat(@DestinationVariable Long roomId, Principal principal) {
        Long userId = requirePrincipalUserId(principal);

        GroupChatResponseDTO.MessageResponse presence =
                groupChatService.buildPresenceBroadcast(roomId, userId);

        messagingTemplate.convertAndSend("/topic/group-chat." + roomId, presence);
    }

    private Long requirePrincipalUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new BusinessException(GeneralErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(GeneralErrorCode.INVALID_PRINCIPAL);
        }
    }
}
