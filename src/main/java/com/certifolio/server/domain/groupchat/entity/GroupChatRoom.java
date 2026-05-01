package com.certifolio.server.domain.groupchat.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 유저 채팅방
 * 채팅방 이름(chattingroomName)으로 unique - 동일한 이름의 방은 1개만 존재
 */
@Entity
@Table(name = "group_chat_rooms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_chat_chattingroom_name", columnNames = { "chattingroom_name" })
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chattingroom_name", nullable = false, length = 100)
    private String chattingroomName;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime lastMessageAt;

    @PrePersist
    protected void onCreate() {
        lastMessageAt = LocalDateTime.now();
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
