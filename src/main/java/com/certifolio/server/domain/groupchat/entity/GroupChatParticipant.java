package com.certifolio.server.domain.groupchat.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 그룹 채팅방 참여자
 * (room, user) 조합 unique - 한 유저는 한 방에 1번만 참여 가능
 */
@Entity
@Table(name = "group_chat_participants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_chat_participant_room_user", columnNames = { "group_chat_room_id", "user_id" })
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_chat_room_id", nullable = false)
    private GroupChatRoom groupChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
