package com.certifolio.server.domain.mentoring.repository;

import com.certifolio.server.domain.mentoring.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 커서 기반 페이지네이션 - cursor(messageId) 이전 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.id < :cursor ORDER BY m.id DESC")
    List<ChatMessage> findByChatRoomIdBeforeCursor(@Param("chatRoomId") Long chatRoomId,
                                                   @Param("cursor") Long cursor,
                                                   Pageable pageable);

    /**
     * 커서 없이 최신 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.id DESC")
    List<ChatMessage> findByChatRoomIdLatest(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}
