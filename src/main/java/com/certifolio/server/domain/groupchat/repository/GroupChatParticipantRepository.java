package com.certifolio.server.domain.groupchat.repository;

import com.certifolio.server.domain.groupchat.entity.GroupChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChatParticipantRepository extends JpaRepository<GroupChatParticipant, Long> {

    boolean existsByGroupChatRoomIdAndUserId(Long groupChatRoomId, Long userId);

    Optional<GroupChatParticipant> findByGroupChatRoomIdAndUserId(Long groupChatRoomId, Long userId);

    long countByGroupChatRoomId(Long groupChatRoomId);

    @Query("SELECT p.groupChatRoom.id, COUNT(p) FROM GroupChatParticipant p " +
            "WHERE p.groupChatRoom.id IN :roomIds GROUP BY p.groupChatRoom.id")
    List<Object[]> countByRoomIds(@Param("roomIds") List<Long> roomIds);

    @Query("SELECT p.groupChatRoom.id FROM GroupChatParticipant p " +
            "WHERE p.user.id = :userId AND p.groupChatRoom.id IN :roomIds")
    List<Long> findJoinedRoomIds(@Param("userId") Long userId, @Param("roomIds") List<Long> roomIds);

    void deleteByGroupChatRoomIdAndUserId(Long groupChatRoomId, Long userId);
}
