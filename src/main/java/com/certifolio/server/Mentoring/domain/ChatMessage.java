package com.certifolio.server.Mentoring.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * 멘토링 세션 내 멘토-멘티 간 채팅 메시지
 *
 * - sequenceNumber: 채팅방 내 메시지 순서 보장용 시퀀스 번호
 * - deliveryStatus: 메시지 전송 상태 (SENT → DELIVERED → READ)
 * - delivered / deliveredAt: ACK 수신 확인 여부 및 시각
 * - retryCount: 재전송 시도 횟수
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chatmsg_room_seq", columnList = "chat_room_id, sequenceNumber"),
        @Index(name = "idx_chatmsg_delivered", columnList = "delivered, sentAt")
})
@Getter
@Setter
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

    /** 채팅방 내 고유 시퀀스 번호 (순서 보장용) */
    @Column(nullable = false)
    private Long sequenceNumber;

    /** 메시지 전송 상태 */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.SENT;

    /** 클라이언트 ACK 수신 여부 */
    @Builder.Default
    @Column(nullable = false)
    private boolean delivered = false;

    /** ACK 수신 시각 */
    private LocalDateTime deliveredAt;

    /** 재전송 시도 횟수 */
    @Builder.Default
    @Column(nullable = false)
    private int retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public enum MessageType {
        TEXT, // 일반 텍스트 메시지
        SYSTEM // 시스템 메시지 (입장, 퇴장 등)
    }

    public enum DeliveryStatus {
        SENT, // 서버 저장 완료
        DELIVERED, // 클라이언트 수신 확인 (ACK)
        READ // 읽음 확인
    }
}
