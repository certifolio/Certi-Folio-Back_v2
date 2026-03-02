package com.certifolio.server.Mentoring.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * 멘토-멘티 간 채팅 메시지
 */
@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public enum MessageType {
        TEXT,
        SYSTEM
    }
}
