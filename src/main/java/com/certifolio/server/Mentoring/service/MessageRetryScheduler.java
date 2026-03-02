package com.certifolio.server.Mentoring.service;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import com.certifolio.server.Mentoring.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 미확인(ACK 미수신) 메시지 자동 재전송 스케줄러
 *
 * - 30초마다 실행
 * - 전송 후 10초 이상 ACK가 오지 않은 메시지를 대상으로 재전송
 * - 최대 5회 재전송 시도, 초과 시 경고 로그 출력
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageRetryScheduler {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /** ACK 대기 시간 (초) - 이 시간이 지나면 재전송 대상 */
    private static final int ACK_TIMEOUT_SECONDS = 10;

    /** 최대 재전송 횟수 */
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * 30초마다 미확인 메시지를 점검하고 재전송
     */
    @Scheduled(fixedDelay = 30000)
    public void retryUndeliveredMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(ACK_TIMEOUT_SECONDS);
        List<ChatMessage> undelivered = chatService.getUndeliveredMessages(threshold);

        if (undelivered.isEmpty()) {
            return;
        }

        log.info("Retry scheduler: {} undelivered message(s) found", undelivered.size());

        for (ChatMessage message : undelivered) {
            if (message.getRetryCount() >= MAX_RETRY_COUNT) {
                log.warn("Message exceeded max retry count: messageId={}, chatRoomId={}, retryCount={}",
                        message.getId(), message.getChatRoom().getId(), message.getRetryCount());
                continue;
            }

            try {
                // 재전송 카운트 증가
                message.incrementRetryCount();

                // 해당 채팅방으로 메시지 재전송
                ChatMessageDTO.MessageResponse response = ChatMessageDTO.MessageResponse.builder()
                        .id(message.getId())
                        .chatRoomId(message.getChatRoom().getId())
                        .senderId(message.getSender().getId())
                        .senderName(message.getSender().getName())
                        .senderProfileImage(message.getSender().getPicture())
                        .content(message.getContent())
                        .type(message.getType().name())
                        .sentAt(message.getSentAt())
                        .sequenceNumber(message.getSequenceNumber())
                        .deliveryStatus(message.getDeliveryStatus().name())
                        .build();

                messagingTemplate.convertAndSend(
                        "/topic/chat." + message.getChatRoom().getId(), response);

                log.info("Message retransmitted: messageId={}, chatRoomId={}, retry={}",
                        message.getId(), message.getChatRoom().getId(), message.getRetryCount());

            } catch (Exception e) {
                log.error("Failed to retransmit message: messageId={}, error={}",
                        message.getId(), e.getMessage());
            }
        }
    }
}
