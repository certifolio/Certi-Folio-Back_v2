package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 세션별 채팅 기록 조회 (시간순)
     */
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(Long sessionId);

    /**
     * 세션별 최근 N개 메시지 조회
     */
    List<ChatMessage> findTop50BySessionIdOrderBySentAtDesc(Long sessionId);

    /**
     * 특정 시간 이후 메시지 개수 (읽지 않은 메시지 수 계산용)
     */
    int countBySessionIdAndSentAtAfter(Long sessionId, LocalDateTime since);
}
