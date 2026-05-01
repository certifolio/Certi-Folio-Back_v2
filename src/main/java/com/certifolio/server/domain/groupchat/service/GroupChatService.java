package com.certifolio.server.domain.groupchat.service;

import com.certifolio.server.domain.groupchat.dto.request.GroupChatRequestDTO;
import com.certifolio.server.domain.groupchat.dto.response.GroupChatResponseDTO;
import com.certifolio.server.domain.groupchat.entity.GroupChatMessage;
import com.certifolio.server.domain.groupchat.entity.GroupChatParticipant;
import com.certifolio.server.domain.groupchat.entity.GroupChatRoom;
import com.certifolio.server.domain.groupchat.entity.GroupMessageType;
import com.certifolio.server.domain.groupchat.repository.GroupChatMessageRepository;
import com.certifolio.server.domain.groupchat.repository.GroupChatParticipantRepository;
import com.certifolio.server.domain.groupchat.repository.GroupChatRoomRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupChatService {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int MAX_ROOMS_PER_USER = 3;

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupChatParticipantRepository participantRepository;
    private final GroupChatMessageRepository messageRepository;
    private final UserService userService;

    /**
     * 유저 채팅방 생성 - 동일한 이름은 1개만 허용, 1인당 최대 3개까지 생성 가능, 생성자는 자동 참여
     */
    @Transactional
    public GroupChatResponseDTO.RoomResponse createRoom(Long userId, GroupChatRequestDTO.CreateRoomRequest request) {
        String chattingroomName = request.chattingroomName() == null ? "" : request.chattingroomName().trim();
        if (chattingroomName.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_INVALID_CHATTINGROOM_NAME);
        }

        if (groupChatRoomRepository.countByCreatorId(userId) >= MAX_ROOMS_PER_USER) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_LIMIT_EXCEEDED);
        }

        if (groupChatRoomRepository.existsByChattingroomName(chattingroomName)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_ALREADY_EXISTS);
        }

        User creator = userService.getUserById(userId);

        GroupChatRoom room = GroupChatRoom.builder()
                .chattingroomName(chattingroomName)
                .description(request.description())
                .creator(creator)
                .build();

        GroupChatRoom savedRoom;
        try {
            savedRoom = groupChatRoomRepository.saveAndFlush(room);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_ALREADY_EXISTS);
        }

        GroupChatParticipant participant = GroupChatParticipant.builder()
                .groupChatRoom(savedRoom)
                .user(creator)
                .build();
        participantRepository.save(participant);

        log.info("유저 채팅방 생성: roomId={}, name={}, by userId={}", savedRoom.getId(), chattingroomName, userId);

        return toRoomResponse(savedRoom, 1L, true);
    }

    /**
     * 전체 채팅방 목록 - joined 여부 포함
     */
    public GroupChatResponseDTO.RoomListResponse getAllRooms(Long currentUserId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findAllOrderByLastMessageAtDesc();
        if (rooms.isEmpty()) {
            return GroupChatResponseDTO.RoomListResponse.builder()
                    .rooms(List.of())
                    .totalCount(0)
                    .build();
        }

        List<Long> roomIds = rooms.stream().map(GroupChatRoom::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = buildParticipantCountMap(roomIds);
        Set<Long> joinedRoomIds = findJoinedRoomIds(currentUserId, roomIds);

        List<GroupChatResponseDTO.RoomResponse> responses = rooms.stream()
                .map(r -> toRoomResponse(r,
                        countMap.getOrDefault(r.getId(), 0L),
                        joinedRoomIds.contains(r.getId())))
                .collect(Collectors.toList());

        return GroupChatResponseDTO.RoomListResponse.builder()
                .rooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 내가 참여 중인 채팅방 목록
     */
    public GroupChatResponseDTO.RoomListResponse getMyRooms(Long userId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findRoomsByUserId(userId);
        if (rooms.isEmpty()) {
            return GroupChatResponseDTO.RoomListResponse.builder()
                    .rooms(List.of())
                    .totalCount(0)
                    .build();
        }

        List<Long> roomIds = rooms.stream().map(GroupChatRoom::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = buildParticipantCountMap(roomIds);

        List<GroupChatResponseDTO.RoomResponse> responses = rooms.stream()
                .map(r -> toRoomResponse(r, countMap.getOrDefault(r.getId(), 0L), true))
                .collect(Collectors.toList());

        return GroupChatResponseDTO.RoomListResponse.builder()
                .rooms(responses)
                .totalCount(responses.size())
                .build();
    }

    /**
     * 채팅방 참여하기 - 참여 처리 + 시스템 메시지 1회 영속화 + lastMessageAt 갱신
     */
    @Transactional
    public GroupChatResponseDTO.JoinResult joinRoom(Long roomId, Long userId) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_NOT_FOUND));

        if (participantRepository.existsByGroupChatRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ALREADY_JOINED);
        }

        User user = userService.getUserById(userId);

        GroupChatParticipant participant = GroupChatParticipant.builder()
                .groupChatRoom(room)
                .user(user)
                .build();
        try {
            participantRepository.saveAndFlush(participant);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ALREADY_JOINED);
        }

        GroupChatMessage systemMessage = GroupChatMessage.builder()
                .groupChatRoom(room)
                .sender(user)
                .content(user.getName() + "님이 채팅에 참가했습니다.")
                .type(GroupMessageType.SYSTEM)
                .build();
        GroupChatMessage savedMessage = messageRepository.save(systemMessage);
        room.updateLastMessageAt();

        log.info("유저 채팅방 참여: roomId={}, userId={}", roomId, userId);

        long count = participantRepository.countByGroupChatRoomId(roomId);

        GroupChatResponseDTO.JoinResponse joinResponse = GroupChatResponseDTO.JoinResponse.builder()
                .roomId(roomId)
                .chattingroomName(room.getChattingroomName())
                .participantCount(count)
                .joinedAt(participant.getCreatedAt())
                .build();

        return GroupChatResponseDTO.JoinResult.builder()
                .joinResponse(joinResponse)
                .systemMessage(toMessageResponse(savedMessage, userId))
                .build();
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        if (!groupChatRoomRepository.existsById(roomId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_NOT_FOUND);
        }
        if (!participantRepository.existsByGroupChatRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ACCESS_DENIED);
        }
        participantRepository.deleteByGroupChatRoomIdAndUserId(roomId, userId);
        log.info("유저 채팅방 나가기: roomId={}, userId={}", roomId, userId);
    }

    /**
     * 메시지 전송 - 참여자만, trim 후 빈값/길이 검증
     */
    @Transactional
    public GroupChatResponseDTO.MessageResponse sendMessage(Long roomId, Long senderId, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException(GeneralErrorCode.INVALID_INPUT);
        }

        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_NOT_FOUND));

        if (!participantRepository.existsByGroupChatRoomIdAndUserId(roomId, senderId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ACCESS_DENIED);
        }

        User sender = userService.getUserById(senderId);

        GroupChatMessage message = GroupChatMessage.builder()
                .groupChatRoom(room)
                .sender(sender)
                .content(trimmed)
                .type(GroupMessageType.TEXT)
                .build();

        GroupChatMessage saved = messageRepository.save(message);
        room.updateLastMessageAt();

        log.info("유저 채팅방 메시지: roomId={}, senderId={}, messageId={}", roomId, senderId, saved.getId());

        return toMessageResponse(saved, senderId);
    }

    /**
     * 재접속/입장 presence 알림 - DB 저장 없이 broadcast 전용 메시지를 생성
     */
    public GroupChatResponseDTO.MessageResponse buildPresenceBroadcast(Long roomId, Long userId) {
        if (!groupChatRoomRepository.existsById(roomId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_NOT_FOUND);
        }
        if (!participantRepository.existsByGroupChatRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ACCESS_DENIED);
        }

        User user = userService.getUserById(userId);

        return GroupChatResponseDTO.MessageResponse.builder()
                .id(null)
                .roomId(roomId)
                .senderId(user.getId())
                .senderName(user.getName())
                .senderProfileImage(user.getPicture())
                .content(user.getName() + "님이 입장했습니다.")
                .type(GroupMessageType.SYSTEM.name())
                .sentAt(LocalDateTime.now())
                .isMine(false)
                .build();
    }

    /**
     * 커서 기반 메시지 조회
     */
    public GroupChatResponseDTO.MessageHistoryResponse getMessages(Long roomId, Long currentUserId, Long cursor, int size) {
        if (!groupChatRoomRepository.existsById(roomId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ROOM_NOT_FOUND);
        }
        if (!participantRepository.existsByGroupChatRoomIdAndUserId(roomId, currentUserId)) {
            throw new BusinessException(GeneralErrorCode.GROUP_CHAT_ACCESS_DENIED);
        }

        PageRequest pageable = PageRequest.of(0, size + 1);
        List<GroupChatMessage> messages = cursor == null
                ? messageRepository.findByRoomIdLatest(roomId, pageable)
                : messageRepository.findByRoomIdBeforeCursor(roomId, cursor, pageable);

        boolean hasNext = messages.size() > size;
        if (hasNext) {
            messages = messages.subList(0, size);
        }

        Collections.reverse(messages);

        List<GroupChatResponseDTO.MessageResponse> responses = messages.stream()
                .map(m -> toMessageResponse(m, currentUserId))
                .collect(Collectors.toList());

        Long nextCursor = hasNext && !messages.isEmpty() ? messages.get(0).getId() : null;

        return GroupChatResponseDTO.MessageHistoryResponse.builder()
                .roomId(roomId)
                .messages(responses)
                .size(responses.size())
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private Map<Long, Long> buildParticipantCountMap(List<Long> roomIds) {
        Map<Long, Long> countMap = new HashMap<>();
        participantRepository.countByRoomIds(roomIds).forEach(row ->
                countMap.put((Long) row[0], (Long) row[1]));
        return countMap;
    }

    private Set<Long> findJoinedRoomIds(Long userId, List<Long> roomIds) {
        if (userId == null || roomIds.isEmpty()) return Set.of();
        return new HashSet<>(participantRepository.findJoinedRoomIds(userId, roomIds));
    }

    private GroupChatResponseDTO.RoomResponse toRoomResponse(GroupChatRoom room, long participantCount, boolean joined) {
        return GroupChatResponseDTO.RoomResponse.builder()
                .roomId(room.getId())
                .chattingroomName(room.getChattingroomName())
                .description(room.getDescription())
                .participantCount(participantCount)
                .joined(joined)
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .build();
    }

    private GroupChatResponseDTO.MessageResponse toMessageResponse(GroupChatMessage message, Long currentUserId) {
        User sender = message.getSender();
        return GroupChatResponseDTO.MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getGroupChatRoom().getId())
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderProfileImage(sender.getPicture())
                .content(message.getContent())
                .type(message.getType().name())
                .sentAt(message.getCreatedAt())
                .isMine(currentUserId != null && sender.getId().equals(currentUserId))
                .build();
    }
}
