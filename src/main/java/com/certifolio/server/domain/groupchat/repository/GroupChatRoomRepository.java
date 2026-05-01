package com.certifolio.server.domain.groupchat.repository;

import com.certifolio.server.domain.groupchat.entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {

    Optional<GroupChatRoom> findByChattingroomName(String chattingroomName);

    boolean existsByChattingroomName(String chattingroomName);

    long countByCreatorId(Long creatorId);

    @Query("SELECT r FROM GroupChatRoom r ORDER BY r.lastMessageAt DESC")
    List<GroupChatRoom> findAllOrderByLastMessageAtDesc();

    @Query("SELECT r FROM GroupChatRoom r " +
            "JOIN GroupChatParticipant p ON p.groupChatRoom.id = r.id " +
            "WHERE p.user.id = :userId " +
            "ORDER BY r.lastMessageAt DESC")
    List<GroupChatRoom> findRoomsByUserId(@Param("userId") Long userId);
}
