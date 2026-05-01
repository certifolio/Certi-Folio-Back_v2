package com.certifolio.server.domain.groupchat.repository;

import com.certifolio.server.domain.groupchat.entity.GroupChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {

    @Query("SELECT m FROM GroupChatMessage m WHERE m.groupChatRoom.id = :roomId AND m.id < :cursor ORDER BY m.id DESC")
    List<GroupChatMessage> findByRoomIdBeforeCursor(@Param("roomId") Long roomId,
                                                    @Param("cursor") Long cursor,
                                                    Pageable pageable);

    @Query("SELECT m FROM GroupChatMessage m WHERE m.groupChatRoom.id = :roomId ORDER BY m.id DESC")
    List<GroupChatMessage> findByRoomIdLatest(@Param("roomId") Long roomId, Pageable pageable);
}
