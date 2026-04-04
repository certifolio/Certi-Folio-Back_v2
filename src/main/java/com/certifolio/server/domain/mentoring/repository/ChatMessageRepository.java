package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방별 채팅 기록 조회 (시간순)
     */
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    /**
     * 채팅방별 최근 50개 메시지 조회
     */
    List<ChatMessage> findTop50ByChatRoomIdOrderBySentAtDesc(Long chatRoomId);
}
