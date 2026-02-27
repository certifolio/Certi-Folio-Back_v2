package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방별 채팅 기록 조회 (시간순)
     */
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    /**
     * 채팅방별 최근 N개 메시지 조회
     */
    List<ChatMessage> findTop50ByChatRoomIdOrderBySentAtDesc(Long chatRoomId);

    /**
     * 특정 시간 이후 메시지 개수 (읽지 않은 메시지 수 계산용)
     */
    int countByChatRoomIdAndSentAtAfter(Long chatRoomId, LocalDateTime since);

    // ===== 시퀀스 번호 / ACK / 동기화 관련 쿼리 =====

    /**
     * 채팅방 내 최대 시퀀스 번호 조회 (다음 시퀀스 계산용)
     */
    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId")
    Long findMaxSequenceNumber(@Param("chatRoomId") Long chatRoomId);

    /**
     * 특정 시퀀스 이후 메시지 조회 (재접속 시 누락 메시지 동기화)
     */
    List<ChatMessage> findByChatRoomIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
            Long chatRoomId, Long sequenceNumber);

    /**
     * ACK 미수신 + 특정 시각 이전에 전송된 메시지 조회 (재전송 대상)
     */
    List<ChatMessage> findByDeliveredFalseAndSentAtBefore(LocalDateTime threshold);

    /**
     * 채팅방별 시퀀스 번호 순서로 메시지 조회
     */
    List<ChatMessage> findByChatRoomIdOrderBySequenceNumberAsc(Long chatRoomId);
}
