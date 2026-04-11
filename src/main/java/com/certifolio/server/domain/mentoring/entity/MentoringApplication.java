package com.certifolio.server.domain.mentoring.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 멘토링 신청 엔티티
 */
@Entity
@Table(name = "mentoring_applications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringApplication extends BaseTimeEntity {

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

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectReason = reason;
    }

    public String getMenteeName() {
        return mentee != null ? mentee.getName() : "Unknown";
    }
}
