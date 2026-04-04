package com.certifolio.server.domain.mentoring.service;

import com.certifolio.server.domain.mentoring.entity.ChatMessage;
import com.certifolio.server.domain.mentoring.entity.ChatRoom;
import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.dto.response.ChatMessageResponseDTO;
import com.certifolio.server.domain.mentoring.repository.ChatMessageRepository;
import com.certifolio.server.domain.mentoring.repository.ChatRoomRepository;
import com.certifolio.server.domain.mentoring.repository.MentorRepository;
import com.certifolio.server.domain.mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.repository.UserRepository;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

        private final ChatMessageRepository chatMessageRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final MentorRepository mentorRepository;
        private final MentoringApplicationRepository mentoringApplicationRepository;
        private final UserRepository userRepository;

        /**
         * 채팅방 생성 또는 기존 채팅방 반환
         */
        @Transactional
        public ChatMessageResponseDTO.ChatRoomResponse getOrCreateChatRoom(Long mentorId, Long userId) {
                return chatRoomRepository.findByMentorIdAndUserId(mentorId, userId)
                                .map(this::toChatRoomResponse)
                                .orElseGet(() -> {
                                        Mentor mentor = mentorRepository.findById(mentorId)
                                                        .orElseThrow(() -> new BusinessException(GeneralErrorCode.MENTOR_NOT_FOUND));

                                        if (mentor.getUser().getId().equals(userId)) {
                                                throw new BusinessException(GeneralErrorCode.SELF_CHAT_NOT_ALLOWED);
                                        }

                                        boolean hasApprovedApplication = mentoringApplicationRepository
                                                        .existsApprovedApplication(userId, mentorId);

                                        if (!hasApprovedApplication) {
                                                throw new BusinessException(GeneralErrorCode.MENTORING_NOT_APPROVED);
                                        }

                                        User user = userRepository.findById(userId)
                                                        .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));

                                        ChatRoom room = ChatRoom.builder()
                                                        .mentor(mentor)
                                                        .user(user)
                                                        .build();

                                        ChatRoom saved = chatRoomRepository.save(room);
                                        log.info("새 채팅방 생성: mentorId={}, userId={}, roomId={}", mentorId, userId, saved.getId());

                                        return toChatRoomResponse(saved);
                                });
        }

        /**
         * 내 채팅방 목록 조회
         */
        @Transactional(readOnly = true)
        public ChatMessageResponseDTO.ChatRoomListResponse getMyChatRooms(Long userId) {
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

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(content)
                                .type(ChatMessage.MessageType.TEXT)
                                .build();

                ChatMessage saved = chatMessageRepository.save(message);

                room.updateLastMessageAt();
                chatRoomRepository.save(room);

                log.info("Chat message saved: chatRoomId={}, senderId={}, messageId={}", chatRoomId, senderId, saved.getId());

                return toResponse(saved, null);
        }

        /**
         * 시스템 메시지 저장
         */
        @Transactional
        public ChatMessageResponseDTO.MessageResponse sendSystemMessage(Long chatRoomId, Long senderId, String content) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CHAT_ROOM_NOT_FOUND));

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(content)
                                .type(ChatMessage.MessageType.SYSTEM)
                                .build();

                ChatMessage saved = chatMessageRepository.save(message);
                return toResponse(saved, null);
        }

        /**
         * 채팅 기록 조회 (시간순)
         */
        @Transactional(readOnly = true)
        public ChatMessageResponseDTO.ChatHistoryResponse getChatHistory(Long chatRoomId, Long currentUserId) {
                validateAndGetRoom(chatRoomId, currentUserId);

                List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

                List<ChatMessageResponseDTO.MessageResponse> messageResponses = messages.stream()
                                .map(msg -> toResponse(msg, currentUserId))
                                .collect(Collectors.toList());

                return ChatMessageResponseDTO.ChatHistoryResponse.builder()
                                .success(true)
                                .chatRoomId(chatRoomId)
                                .messages(messageResponses)
                                .totalCount(messageResponses.size())
                                .build();
        }

        /**
         * 최근 메시지 조회 (최대 50개)
         */
        @Transactional(readOnly = true)
        public ChatMessageResponseDTO.ChatHistoryResponse getRecentMessages(Long chatRoomId, Long currentUserId) {
                validateAndGetRoom(chatRoomId, currentUserId);

                List<ChatMessage> messages = chatMessageRepository.findTop50ByChatRoomIdOrderBySentAtDesc(chatRoomId);
                Collections.reverse(messages);

                List<ChatMessageResponseDTO.MessageResponse> messageResponses = messages.stream()
                                .map(msg -> toResponse(msg, currentUserId))
                                .collect(Collectors.toList());

                return ChatMessageResponseDTO.ChatHistoryResponse.builder()
                                .success(true)
                                .chatRoomId(chatRoomId)
                                .messages(messageResponses)
                                .totalCount(messageResponses.size())
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
                                .sentAt(message.getSentAt())
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
