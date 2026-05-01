package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.ChatMessage;
import com.certifolio.server.domain.mentoring.entity.ChatRoom;
import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.entity.MessageType;
import com.certifolio.server.domain.mentoring.dto.response.ChatMessageResponseDTO;
import com.certifolio.server.domain.mentoring.repository.ChatMessageRepository;
import com.certifolio.server.domain.mentoring.repository.ChatRoomRepository;
import com.certifolio.server.domain.mentoring.repository.MentorRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

        private final ChatMessageRepository chatMessageRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final MentorRepository mentorRepository;
        private final MentoringApplicationRepository mentoringApplicationRepository;
        private final UserService userService;

        /**
         * 채팅방 생성 또는 기존 채팅방 반환
         */
        @Transactional
        public ChatMessageResponseDTO.ChatRoomResponse getOrCreateChattingRoom(Long mentorId, Long userId, Long menteeUserId) {
                Mentor mentor = mentorRepository.findById(mentorId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

                boolean requesterIsMentor = mentor.getUser().getId().equals(userId);
                Long chatUserId = resolveChatUserId(mentor, userId, menteeUserId, requesterIsMentor);

                return chatRoomRepository.findByMentorIdAndUserId(mentorId, chatUserId)
                                .map(this::toChatRoomResponse)
                                .orElseGet(() -> {
                                        boolean hasApprovedApplication = mentoringApplicationRepository
                                                        .existsApprovedApplicationByMenteeAndMentor(chatUserId, mentorId);

                                        if (!hasApprovedApplication) {
                                                throw new BusinessException(GeneralErrorCode.MENTORING_NOT_APPROVED);
                                        }

                                        User user = userService.getUserById(chatUserId);

                                        ChatRoom room = ChatRoom.builder()
                                                        .mentor(mentor)
                                                        .user(user)
                                                        .build();

                                        ChatRoom saved = chatRoomRepository.save(room);
                                        log.info("새 채팅방 생성: mentorId={}, userId={}, roomId={}", mentorId, userId, saved.getId());

                                        return toChatRoomResponse(saved);
                                });
        }

        private Long resolveChatUserId(Mentor mentor, Long requesterUserId, Long menteeUserId, boolean requesterIsMentor) {
                if (!requesterIsMentor) {
                        return requesterUserId;
                }

                if (menteeUserId == null || menteeUserId.equals(requesterUserId)) {
                        throw new BusinessException(GeneralErrorCode.MENTORING_NOT_APPROVED);
                }

                return menteeUserId;
        }

        /**
         * 내 채팅방 목록 조회
         */
        public ChatMessageResponseDTO.ChatRoomListResponse getMyChatRooms(Long userId) {
                log.info("채팅방 목록 조회: userId={}", userId);
                List<ChatRoom> rooms = chatRoomRepository.findMyRooms(userId);

                List<ChatMessageResponseDTO.ChatRoomResponse> roomResponses = rooms.stream()
                                .map(this::toChatRoomResponse)
                                .collect(Collectors.toList());

                return ChatMessageResponseDTO.ChatRoomListResponse.builder()
                                .success(true)
                                .rooms(roomResponses)
                                .totalCount(roomResponses.size())
                                .build();
        }

        /**
         * 메시지 저장 및 응답 DTO 반환
         */
        @Transactional
        public ChatMessageResponseDTO.MessageResponse sendMessage(Long chatRoomId, Long senderId, String content) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CHAT_ROOM_NOT_FOUND));

                if (!isRoomParticipant(room, senderId)) {
                        throw new BusinessException(GeneralErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }

                User sender = room.getUser().getId().equals(senderId)
                                ? room.getUser()
                                : room.getMentor().getUser();

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(content)
                                .type(MessageType.TEXT)
                                .build();

                ChatMessage saved = chatMessageRepository.save(message);

                room.updateLastMessageAt();

                log.info("Chat message saved: chatRoomId={}, senderId={}, messageId={}", chatRoomId, senderId, saved.getId());

                return toResponse(saved, null);
        }

        /**
         * 채팅방 입장 시스템 메시지 저장
         */
        @Transactional
        public ChatMessageResponseDTO.MessageResponse sendSystemMessage(Long chatRoomId, Long senderId) {
                log.info("시스템 메시지 전송: chatRoomId={}, senderId={}", chatRoomId, senderId);
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CHAT_ROOM_NOT_FOUND));

                if (!isRoomParticipant(room, senderId)) {
                        throw new BusinessException(GeneralErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }

                User sender = room.getUser().getId().equals(senderId)
                                ? room.getUser()
                                : room.getMentor().getUser();

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(sender.getName() + "님이 채팅에 참가했습니다.")
                                .type(MessageType.SYSTEM)
                                .build();

                ChatMessage saved = chatMessageRepository.save(message);
                return toResponse(saved, null);
        }

        /**
         * 커서 기반 페이지네이션 메시지 조회
         * cursor가 null이면 최신 메시지부터, 있으면 해당 id 이전 메시지 조회
         */
        public ChatMessageResponseDTO.ChatHistoryResponse getMessages(Long chatRoomId, Long currentUserId, Long cursor, int size) {
                log.info("메시지 조회: chatRoomId={}, userId={}, cursor={}, size={}", chatRoomId, currentUserId, cursor, size);
                validateAndGetRoom(chatRoomId, currentUserId);

                // size+1개 조회해서 hasNext 판단
                PageRequest pageable = PageRequest.of(0, size + 1);
                List<ChatMessage> messages = cursor == null
                                ? chatMessageRepository.findByChatRoomIdLatest(chatRoomId, pageable)
                                : chatMessageRepository.findByChatRoomIdBeforeCursor(chatRoomId, cursor, pageable);

                boolean hasNext = messages.size() > size;
                if (hasNext) {
                        messages = messages.subList(0, size);
                }

                Collections.reverse(messages); // 오래된 순으로 정렬

                List<ChatMessageResponseDTO.MessageResponse> messageResponses = messages.stream()
                                .map(msg -> toResponse(msg, currentUserId))
                                .collect(Collectors.toList());

                Long nextCursor = hasNext ? messages.get(0).getId() : null;

                return ChatMessageResponseDTO.ChatHistoryResponse.builder()
                                .chatRoomId(chatRoomId)
                                .messages(messageResponses)
                                .size(messageResponses.size())
                                .nextCursor(nextCursor)
                                .hasNext(hasNext)
                                .build();
        }

        private ChatMessageResponseDTO.MessageResponse toResponse(ChatMessage message, Long currentUserId) {
                User sender = message.getSender();
                return ChatMessageResponseDTO.MessageResponse.builder()
                                .id(message.getId())
                                .chatRoomId(message.getChatRoom().getId())
                                .senderId(sender.getId())
                                .senderName(sender.getName())
                                .senderProfileImage(sender.getPicture())
                                .content(message.getContent())
                                .type(message.getType().name())
                                .sentAt(message.getCreatedAt())
                                .isMine(currentUserId != null && sender.getId().equals(currentUserId))
                                .build();
        }

        private ChatRoom validateAndGetRoom(Long chatRoomId, Long currentUserId) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CHAT_ROOM_NOT_FOUND));

                if (currentUserId != null && !isRoomParticipant(room, currentUserId)) {
                        throw new BusinessException(GeneralErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }

                return room;
        }

        private boolean isRoomParticipant(ChatRoom room, Long userId) {
                return room.getUser().getId().equals(userId)
                                || room.getMentor().getUser().getId().equals(userId);
        }

        private ChatMessageResponseDTO.ChatRoomResponse toChatRoomResponse(ChatRoom room) {
                Mentor mentor = room.getMentor();
                User mentorUser = mentor.getUser();
                User chatUser = room.getUser();

                return ChatMessageResponseDTO.ChatRoomResponse.builder()
                                .chatRoomId(room.getId())
                                .mentorId(mentor.getId())
                                .mentorUserId(mentorUser.getId())
                                .mentorName(mentorUser.getName())
                                .mentorProfileImage(mentorUser.getPicture())
                                .mentorCompany(mentor.getCompany())
                                .userId(chatUser.getId())
                                .userName(chatUser.getName())
                                .userProfileImage(chatUser.getPicture())
                                .createdAt(room.getCreatedAt())
                                .lastMessageAt(room.getLastMessageAt())
                                .build();
        }
}
