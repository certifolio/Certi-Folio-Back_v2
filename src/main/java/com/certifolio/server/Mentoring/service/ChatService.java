package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import com.certifolio.server.Mentoring.domain.MentoringSession;
import com.certifolio.server.Mentoring.dto.ChatMessageDTO;
import com.certifolio.server.Mentoring.repository.ChatMessageRepository;
import com.certifolio.server.Mentoring.repository.MentoringSessionRepository;
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
    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * 메시지 저장 및 응답 DTO 반환
     */
    @Transactional
    public ChatMessageDTO.MessageResponse sendMessage(Long sessionId, Long senderId, String content) {
        MentoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("멘토링 세션을 찾을 수 없습니다."));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .type(ChatMessage.MessageType.TEXT)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("Chat message saved: sessionId={}, senderId={}, messageId={}", sessionId, senderId, saved.getId());

        return toResponse(saved, null);
    }

    /**
     * 시스템 메시지 저장 (입장/퇴장 알림 등)
     */
    @Transactional
    public ChatMessageDTO.MessageResponse sendSystemMessage(Long sessionId, Long senderId, String content) {
        MentoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("멘토링 세션을 찾을 수 없습니다."));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .type(ChatMessage.MessageType.SYSTEM)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return toResponse(saved, null);
    }

    /**
     * 특정 세션의 채팅 기록 조회
     */
    @Transactional(readOnly = true)
    public ChatMessageDTO.ChatHistoryResponse getChatHistory(Long sessionId, Long currentUserId) {
        // 세션 존재 확인
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("멘토링 세션을 찾을 수 없습니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId);

        List<ChatMessageDTO.MessageResponse> messageResponses = messages.stream()
                .map(msg -> toResponse(msg, currentUserId))
                .collect(Collectors.toList());

        return ChatMessageDTO.ChatHistoryResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .messages(messageResponses)
                .totalCount(messageResponses.size())
                .build();
    }

    /**
     * 최근 메시지 조회 (최대 50개, 최신순 → 시간순으로 정렬)
     */
    @Transactional(readOnly = true)
    public ChatMessageDTO.ChatHistoryResponse getRecentMessages(Long sessionId, Long currentUserId) {
        List<ChatMessage> messages = chatMessageRepository.findTop50BySessionIdOrderBySentAtDesc(sessionId);

        // 최신순으로 가져온 것을 시간순으로 뒤집기
        Collections.reverse(messages);

        List<ChatMessageDTO.MessageResponse> messageResponses = messages.stream()
                .map(msg -> toResponse(msg, currentUserId))
                .collect(Collectors.toList());

        return ChatMessageDTO.ChatHistoryResponse.builder()
                .success(true)
                .sessionId(sessionId)
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
                .sessionId(message.getSession().getId())
                .senderId(sender.getId())
                .senderName(sender.getNickname() != null ? sender.getNickname() : sender.getName())
                .senderProfileImage(sender.getPicture())
                .content(message.getContent())
                .type(message.getType().name())
                .sentAt(message.getSentAt())
                .isMine(currentUserId != null && sender.getId().equals(currentUserId))
                .build();
    }
}
