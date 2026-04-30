package com.certifolio.server.domain.mentoring.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * 멘토링 세션 엔티티
 */
@Entity
@Table(name = "mentoring_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @Column(nullable = false)
    private String topic; // 멘토링 주제

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PENDING;

    private LocalDate startDate; // 시작일

    public void updateStatus(SessionStatus status) {
        this.status = status;
    }
}
