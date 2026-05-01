package com.certifolio.server.domain.groupchat.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 그룹 채팅 메시지
 */
@Entity
@Table(name = "group_chat_messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_chat_room_id", nullable = false)
    private GroupChatRoom groupChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMessageType type = GroupMessageType.TEXT;
}
