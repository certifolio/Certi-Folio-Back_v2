package com.certifolio.server.domain.mentoring.repository;

import com.certifolio.server.domain.mentoring.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByMentorIdAndUserId(Long mentorId, Long userId);

    @Query("SELECT r FROM ChatRoom r WHERE r.user.id = :userId OR r.mentor.user.id = :userId ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findMyRooms(@Param("userId") Long userId);
}
