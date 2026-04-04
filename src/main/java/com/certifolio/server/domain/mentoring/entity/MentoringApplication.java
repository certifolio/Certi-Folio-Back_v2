package com.certifolio.server.domain.mentoring.entity;

import com.certifolio.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 멘토링 신청 엔티티
 */
@Entity
@Table(name = "mentoring_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private String topic; // 멘토링 주제

    @Column(length = 1000)
    private String description; // 상세 설명

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private String rejectReason; // 거절 사유

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

    // Helper methods
    public String getMenteeName() {
        return mentee != null ? mentee.getName() : "Unknown";
    }
}
