package com.certifolio.server.domain.mentoring.entity;

import com.certifolio.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 멘토링 세션 엔티티
 */
@Entity
@Table(name = "mentoring_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringSession {

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
