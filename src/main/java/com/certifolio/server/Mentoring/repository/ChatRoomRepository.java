package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 멘토-유저 쌍으로 기존 채팅방 조회
     */
    Optional<ChatRoom> findByMentorIdAndUserId(Long mentorId, Long userId);

    /**
     * 내 채팅방 목록 조회 (유저이거나 멘토의 User인 경우)
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.user.id = :userId OR cr.mentor.user.id = :userId " +
            "ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findMyRooms(@Param("userId") Long userId);
}
