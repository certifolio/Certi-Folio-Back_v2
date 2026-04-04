package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import com.certifolio.server.Mentoring.domain.ChatRoom;
import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.dto.ChatMessageDTO;
import com.certifolio.server.Mentoring.repository.ChatMessageRepository;
import com.certifolio.server.Mentoring.repository.ChatRoomRepository;
import com.certifolio.server.Mentoring.repository.MentorRepository;
import com.certifolio.server.Mentoring.repository.MentoringApplicationRepository;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
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
         * 승인된 멘토링 관계가 있는 경우에만 채팅방 생성 가능
         */
        @Transactional
        public ChatMessageDTO.ChatRoomResponse getOrCreateChatRoom(Long mentorId, Long userId) {
                // 기존 채팅방이 있으면 바로 반환
                return chatRoomRepository.findByMentorIdAndUserId(mentorId, userId)
                                .map(this::toChatRoomResponse)
                                .orElseGet(() -> {
                                        Mentor mentor = mentorRepository.findById(mentorId)
                                                        .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다."));

                                        // 멘토 본인과는 채팅방 생성 불가
                                        if (mentor.getUser().getId().equals(userId)) {
                                                throw new IllegalStateException("멘토 본인과는 채팅방을 생성할 수 없습니다.");
                                        }

                                        // 승인된 멘토링 신청이 있는지 확인
                                        boolean hasApprovedApplication = mentoringApplicationRepository
                                                        .existsApprovedApplication(userId, mentorId);

                                        if (!hasApprovedApplication) {
                                                throw new IllegalStateException("승인된 멘토링 관계가 있어야 채팅이 가능합니다.");
                                        }

                                        User user = userRepository.findById(userId)
                                                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                                        ChatRoom room = ChatRoom.builder()
                                                        .mentor(mentor)
                                                        .user(user)
                                                        .build();

                                        ChatRoom saved = chatRoomRepository.save(room);
                                        log.info("새 채팅방 생성: mentorId={}, userId={}, roomId={}", mentorId, userId,
                                                        saved.getId());

                                        return toChatRoomResponse(saved);
                                });
        }

        /**
         * 내 채팅방 목록 조회
         */
        @Transactional(readOnly = true)
        public ChatMessageDTO.ChatRoomListResponse getMyChatRooms(Long userId) {
                List<ChatRoom> rooms = chatRoomRepository.findMyRooms(userId);

                List<ChatMessageDTO.ChatRoomResponse> roomResponses = rooms.stream()
                                .map(this::toChatRoomResponse)
                                .collect(Collectors.toList());

                return ChatMessageDTO.ChatRoomListResponse.builder()
                                .success(true)
                                .rooms(roomResponses)
                                .totalCount(roomResponses.size())
                                .build();
        }

        /**
         * 메시지 저장 및 응답 DTO 반환
         */
        @Transactional
        public ChatMessageDTO.MessageResponse sendMessage(Long chatRoomId, Long senderId, String content) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

                // 채팅방 참여자인지 검증
                if (!isRoomParticipant(room, senderId)) {
                        throw new IllegalStateException("해당 채팅방에 참여 권한이 없습니다.");
                }

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                ChatMessage message = ChatMessage.builder()
                                .chatRoom(room)
                                .sender(sender)
                                .content(content)
                                .type(ChatMessage.MessageType.TEXT)
                                .build();

                ChatMessage saved = chatMessageRepository.save(message);

                // 채팅방 마지막 메시지 시간 업데이트
                room.updateLastMessageAt();
                chatRoomRepository.save(room);

                log.info("Chat message saved: chatRoomId={}, senderId={}, messageId={}",
                                chatRoomId, senderId, saved.getId());

                return toResponse(saved, null);
        }

        /**
         * 시스템 메시지 저장 (입장/퇴장 알림 등)
         */
        @Transactional
        public ChatMessageDTO.MessageResponse sendSystemMessage(Long chatRoomId, Long senderId,
                        String content) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

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
         * 특정 채팅방의 채팅 기록 조회 (시간순 정렬)
         */
        @Transactional(readOnly = true)
        public ChatMessageDTO.ChatHistoryResponse getChatHistory(Long chatRoomId, Long currentUserId) {
                validateAndGetRoom(chatRoomId, currentUserId);

                List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

                List<ChatMessageDTO.MessageResponse> messageResponses = messages.stream()
                                .map(msg -> toResponse(msg, currentUserId))
                                .collect(Collectors.toList());

                return ChatMessageDTO.ChatHistoryResponse.builder()
                                .success(true)
                                .chatRoomId(chatRoomId)
                                .messages(messageResponses)
                                .totalCount(messageResponses.size())
                                .build();
        }

        /**
         * 최근 메시지 조회 (최대 50개, 최신순 → 시간순으로 정렬)
         */
        @Transactional(readOnly = true)
        public ChatMessageDTO.ChatHistoryResponse getRecentMessages(Long chatRoomId, Long currentUserId) {
                validateAndGetRoom(chatRoomId, currentUserId);

                List<ChatMessage> messages = chatMessageRepository.findTop50ByChatRoomIdOrderBySentAtDesc(chatRoomId);

                // 최신순으로 가져온 것을 시간순으로 뒤집기
                Collections.reverse(messages);

                List<ChatMessageDTO.MessageResponse> messageResponses = messages.stream()
                                .map(msg -> toResponse(msg, currentUserId))
                                .collect(Collectors.toList());

                return ChatMessageDTO.ChatHistoryResponse.builder()
                                .success(true)
                                .chatRoomId(chatRoomId)
                                .messages(messageResponses)
                                .totalCount(messageResponses.size())
                                .build();
        }

        /**
         * ChatMessage → MessageResponse 변환
         */
        private ChatMessageDTO.MessageResponse toResponse(ChatMessage message, Long currentUserId) {
                User sender = message.getSender();
                return ChatMessageDTO.MessageResponse.builder()
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

        /**
         * 채팅방 조회 및 참여자 검증 (공통 로직)
         */
        private ChatRoom validateAndGetRoom(Long chatRoomId, Long currentUserId) {
                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

                if (currentUserId != null && !isRoomParticipant(room, currentUserId)) {
                        throw new IllegalStateException("해당 채팅방에 참여 권한이 없습니다.");
                }

                return room;
        }

        /**
         * 채팅방 참여자(멘토 또는 멘티)인지 확인
         */
        private boolean isRoomParticipant(ChatRoom room, Long userId) {
                return room.getUser().getId().equals(userId)
                                || room.getMentor().getUser().getId().equals(userId);
        }

        /**
         * ChatRoom → ChatRoomResponse 변환
         */
        private ChatMessageDTO.ChatRoomResponse toChatRoomResponse(ChatRoom room) {
                Mentor mentor = room.getMentor();
                User mentorUser = mentor.getUser();
                User chatUser = room.getUser();

                return ChatMessageDTO.ChatRoomResponse.builder()
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
